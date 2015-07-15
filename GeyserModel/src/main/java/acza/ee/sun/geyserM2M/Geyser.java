/* --------------------------------------------------------------------------------------------------------
 * DATE:	13 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Software implementation of Phillip's model
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Geyser {
	
	private static final Logger logger = LogManager.getLogger(Geyser.class);
	
	//Geyser constants
	private static final double rho = 1000; //Denisity of water
	private static final double c = 1.1611; //Specific heat capacity of water	
	
	//Geyser parameters
	private boolean DISABLE_TWO_NODE;	//Selector for 1-node or 2-node state
	private double R; 					//EWH thermal resistance
	private double G; 					//EWH thermal conductance
	private double TANK_LENGTH; 		//Length of EWH in meters
	private double TANK_VOLUME; 		//Volume of EWH in liters
	private double TANK_RADIUS;			//Radius of tank in meters
	private double TANK_AREA;
	private double TIMECONSTANT;		//
	private double ELEMENT_POWER;		//Element power rating in Watt
	private double THRESHOLD_VOLUME; 	//Threshold volume for two-node state transition
	private double INLET_TEMPERATURE;	//Inlet temperature
	private enum Orientation {VERT, HORZ};
	private Orientation ORIENTATION;	//Orientation of EWH determines exposed surface area for node thermal losses 
	
	//Geyser state variables
	private enum NodeState {ONE, TWO};
	private NodeState node_state;
	
	private double t_lower;		//2-Node lower temperature
	private double t_upper; 	//2-Node upper temperature
	private double t_inside;	//1-Node inside temperature
	private double t_ambient;	//Ambient temperature outside EWH
	private double v_upper;		//2-Node lower volume
	private double v_lower;		//2-Node upper volume
	private double r_lower;		//2-Node lower thermal resistance 
	private double r_upper;		//2-Node upper thermal resistance 
	private boolean element_state; //(2)

	
	//-----------------------------------------------------------------------------------------------------------------------------------------
	
	
	public Geyser(){
		
		//Set EWH paramaters
		DISABLE_TWO_NODE = false;
		TANK_LENGTH = 1; 		
		TANK_VOLUME = 150; 	
		TANK_RADIUS = Math.sqrt((TANK_VOLUME/1000)/(Math.PI*TANK_LENGTH));
		TANK_AREA = 2*Math.PI*TANK_RADIUS*TANK_LENGTH + 2*Math.PI*TANK_RADIUS*TANK_RADIUS;
		R = 17.992; 			
		G = (1 / R)*(1000 / (24*TANK_AREA));	
		TIMECONSTANT = (c / 1000)*TANK_VOLUME*R;
		ELEMENT_POWER = 3000;	
		THRESHOLD_VOLUME = 30; 				
		INLET_TEMPERATURE = 20;		
		ORIENTATION = Orientation.HORZ; //(5)
		
		//Set initial EWH variable values. 
		node_state = NodeState.ONE;
		t_lower = t_upper = t_inside = 55;
		v_upper = TANK_VOLUME;
		element_state = true;
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Steps the EWH state given a time in seconds
	 * @param step_seconds
	 * @return change in system energy:  (element gain) - (thermal loss)
	 */
	public double stepTime(long step_seconds){
		
		double termal_energy_loss = 0;
		double element_energy_added = 0;
		
		if(DISABLE_TWO_NODE)
			node_state = NodeState.ONE;
		
		switch(node_state){
		case ONE:
			//Calculate energy input by element and update INSIDE temperature
			if(element_state){
				element_energy_added = ELEMENT_POWER*step_seconds; 
				t_inside += deltaTemperature(element_energy_added, TANK_VOLUME);
			}
			
			//Calculate change in INSIDE temperature and then thermal losses
			double t_inside_before = t_inside;
			t_inside = t_ambient + (t_inside_before - t_ambient)*Math.exp((-1.0 * step_seconds)/(c*rho*TANK_VOLUME*R)); //(3)
			termal_energy_loss = c*rho*(0.001*TANK_VOLUME)*(t_inside_before - t_inside); //(4)
			
			
			break;
		case TWO:
			//Calculate energy input by element and update LOWER NODE temperature
			if(element_state){
				element_energy_added = ELEMENT_POWER*step_seconds;
				t_lower += deltaTemperature(element_energy_added, v_lower);
				
				//Assume that time steps will be small enough that error will be negligible. (6)
				if(t_lower >= t_upper){
					t_inside = t_lower = t_upper;
					node_state = NodeState.ONE;
				}
			}
			
			//Calculate thermal losses and update LOWER and UPPER NODE temperatures
			switch(ORIENTATION){
			case HORZ:
				//Calculate thermal resistance for both node segments
				NewtonRaphson nr = new NewtonRaphson(TANK_RADIUS, TANK_LENGTH);
				double upper_area = nr.surfaceArea(nr.calculateArcLength(v_upper));
				r_upper = (1 / R)*(1000 / (24*upper_area));
				r_lower = (1 / R)*(1000 / (24*(TANK_AREA-upper_area))); // (5) TANK_AREA-upper_area??
				
				//Update t_lower and t_upper and calculate thermal losses using resistances
				double t_lower_before = t_lower;
				double t_upper_before = t_upper;
				t_lower = t_ambient + (t_lower_before - t_ambient)*Math.exp((-1.0 * step_seconds)/(c*rho*TANK_VOLUME*r_lower)); //(3)
				t_upper = t_ambient + (t_upper_before - t_ambient)*Math.exp((-1.0 * step_seconds)/(c*rho*TANK_VOLUME*r_upper)); //(3)
				termal_energy_loss += c*rho*(0.001*TANK_VOLUME)*(t_lower_before - t_inside);
				termal_energy_loss += c*rho*(0.001*TANK_VOLUME)*(t_upper_before - t_inside);
				break;
				
			case VERT:
				logger.error("Vertical orientation not yet impelemented");
				break;
			}
			
			break;
		}
		
		//logger.info("Time step of "+ step_seconds +" sec. " + String.format("Element gain: %.2f -  Thermal loss: %.2f watt.", element_energy_added, termal_energy_loss));
		return  element_energy_added - termal_energy_loss;
	}

	/**
	 * Steps the EWH state given a volume of hot water used
	 * @param usage_litres volume of hot water used
	 * @return energy_usage enthalpy of used water (energy that leaves system due to event)
	 */
	public double stepUsage(double usage_litres){
		
		double energy_usage = 0;
		
		if(node_state != NodeState.TWO && usage_litres >= THRESHOLD_VOLUME)
			node_state = NodeState.TWO;
		
		if(DISABLE_TWO_NODE)
			node_state = NodeState.ONE;
			
		switch(node_state){
		case ONE:
			//Calculate energy leaving in the used water
			energy_usage = waterEnthalpy(t_inside, usage_litres);
			
			//Update t_inside
			t_inside = ((TANK_VOLUME - usage_litres)/TANK_VOLUME) * (t_inside - INLET_TEMPERATURE) + INLET_TEMPERATURE; //(1)
			break;
		case TWO:
			//Calculate energy leaving in the used water
			energy_usage = waterEnthalpy(t_upper, usage_litres);
			
			//Update t_lower, v_lower, v_upper
			t_lower = (v_lower/(v_lower+usage_litres) * (t_lower - INLET_TEMPERATURE) + INLET_TEMPERATURE); //Error in Philip's code. (1)(5) 
			v_upper -= usage_litres;
			v_lower +=  usage_litres;
			break;
			
			//t_upper should not be updated?? Ask Philip
		}
		
		//logger.info("Usage event of "+ usage_litres +" liters: " + String.format("%.2f", energy_usage)+ " Watt");
		return energy_usage;
	}
	
	
	//----------------------------------------------------- Useful equations -----------------------------------------------

	/**
	 * Calculates the energy in a volume of water due to its temperature (pressure ignored).
	 * It is assumed that INLET_TEMPERATURE represents ZERO energy
	 * @param water_temperature
	 * @param liters_water
	 * @return energy in water volume relative to inlet temperature
	 */
	private double waterEnthalpy(double water_temperature, double liters_water){
		return c * rho *(liters_water * 0.001) * (water_temperature - INLET_TEMPERATURE);	//WHY DOES PHILIP NOT INCLUDE RHO?! (5)
	}
	/**
	 * Calculates the CHANGE in temperature of a volume of water due to adding/removing energy.
	 * @param energy energy added(+) or removed(-)
	 * @param liters_water volume of water
	 * @return delta T
	 */
	private double deltaTemperature(double energy, double liters_water){
		return (energy/1000)/(c * rho * (liters_water * 0.001)); //WHY devide by 1000! Philip does it as well but it does not make sense. (5)
	}
	
	//----------------------------------------------------- GETTERS AND SETTER-----------------------------------------------
	
	public void disableTwoNode(boolean request){
		DISABLE_TWO_NODE = request;
	}
	
	public double getLowerTemperature() {
		return t_lower;
	}

	public double getUpperTemperature() {
		return t_upper;
	}

	public double getInsideTemperature() {
		return t_inside;
	}

	public double getLowerVolume() {
		return v_lower;
	}
	
	public double getUpperVolume() {
		return v_upper;
	}
	
	public String getNodeState(){
		return node_state.toString();
	}
	
	public String toString(){

		switch(node_state){
		case ONE:
			return  "***************************\n"
			+"Node state: \t" + node_state.toString() + "\n"
			+ "t_inside: \t" + String.format("%.2f",t_inside) + "\n"
			+ "---------- N/A ----------\n"
			+ "t_lower: \t" + String.format("%.2f",t_lower) + "\n"
			+ "t_upper: \t" + String.format("%.2f",t_upper) + "\n"
			+ "v_lower: \t" + String.format("%.2f",v_lower) + "\n"
			+ "v_upper: \t" + String.format("%.2f",v_upper) + "\n";
		case TWO:
			return "***************************\n"
			+ "Node state: \t" + node_state.toString() + "\n"
			+ "t_lower: \t" + String.format("%.2f",t_lower) + "\n"
			+ "t_upper: \t" + String.format("%.2f",t_upper) + "\n"
			+ "v_lower: \t" + String.format("%.2f",v_lower) + "\n"
			+ "v_upper: \t" + String.format("%.2f",v_upper) + "\n"
			+ "---------- N/A ----------\n"
			+ "t_inside: \t" + String.format("%.2f",t_inside) + "\n";
		default:
			return "Error with node_state: " + node_state.toString();

		}
	}
	
	public String toJSON(){
		return "{"
				+"\"NodeState\":" + "\"" + node_state.toString() + "\"" + ", "
				+"\"t_lower\":" + String.format("%.2f",t_lower)+ ", "
				+"\"t_upper\":" + String.format("%.2f",t_upper)+ ", "
				+"\"t_inside\":" + String.format("%.2f",t_inside)+ ", "
				+"\"v_lower\":" + String.format("%.2f",v_lower)+ ", "
				+"\"v_upper\":" + String.format("%.2f",v_upper)
				+ "}";
	}

}

/*
 * (1)
 * Equation 3
 * - Carefully note that V_hot is the REMAINING temperature. 
 * 
 * (2)
 * Not sure if a thermostat should be part of the model. 
 * 
 * 
 * (3)
 * Equation 9
 * 
 * (4)
 * Equation 11
 * 
 * (5)
 * Questions to Philip/Thinus.
 * 
 * (6)
 * The more correct way would be to first calculate if the added element energy will cause the lower node temperature to 
 * excede that of the upper node. And if it would, then use a piecewise function to determine energy gains before entering 
 * ONE node state again.
 * 
 * 
 */