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
	private double DEADBAND;			//Thermostat deadband in degrees C
	private double INLET_TEMPERATURE;	//Inlet temperature
	private double SETPOINT_TEMPERATURE;//EWH set-point temperature
	
	//Geyser state variables
	private enum NodeState {ONE, TWO};
	private NodeState node_state;
	
	private double t_lower;		//2-Node lower temperature
	private double t_upper; 	//2-Node upper temperature
	private double t_inside;	//1-Node inside temperature
	private double v_upper;		//2-Node lower volume
	private double v_lower;		//2-Node upper volume
	private double r_lower;		//2-Node lower thermal resistance 
	private double r_upper;		//2-Node upper thermal resistance 
	
	private boolean element_state;
	
	//private double e_lower; //2-Node lower energy
	//private double e_upper; //2-Node upper energy
	//private double e_inside; //1-Node total inside energy
	
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
		DEADBAND = 2;	
		INLET_TEMPERATURE = 20;		
		SETPOINT_TEMPERATURE = 55;
		
		//Set initial EWH variable values. 
		node_state = NodeState.ONE;
		t_lower = t_upper = t_inside = 55;
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Steps the EWH state given a time in seconds
	 * @param step_seconds
	 * @return termal_energy_loss energy loss due to thermal radiation
	 */
	public double stepTime(long step_seconds){
		
		double termal_energy_loss = 0;
		
		if(DISABLE_TWO_NODE)
			node_state = NodeState.ONE;
		
		switch(node_state){
		case ONE:
			break;
		case TWO:
			break;
		}
		
		return termal_energy_loss;
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
			t_inside = ((TANK_VOLUME - usage_litres)/TANK_VOLUME) * (t_inside - INLET_TEMPERATURE) + INLET_TEMPERATURE;
			break;
		case TWO:
			//Calculate energy leaving in the used water
			energy_usage = waterEnthalpy(t_upper, usage_litres);
			
			//Update t_lower, v_lower, v_upper
			t_lower = (v_lower/(v_lower+usage_litres) * (t_lower -INLET_TEMPERATURE) + INLET_TEMPERATURE); //Error in Philip's code. Carefully note Equation 3 
			v_upper -= usage_litres;
			v_lower +=  usage_litres;
			break;
			
			//t_upper should not be updated?? Ask Philip
		}
		
		return energy_usage;
	}
	
	
	//----------------------------------------------------- Useful equations -----------------------------------------------

	/**
	 * Calculate the energy in a volume of water due to its temperature (pressure ignored).
	 * It is assumed that INLET_TEMPERATURE represents ZERO energy
	 * @param water_temperature
	 * @param liters_water
	 * @return
	 */
	private double waterEnthalpy(double water_temperature, double liters_water){
		return c * (liters_water * 0.001) * (water_temperature - INLET_TEMPERATURE);
	}
	
	//----------------------------------------------------- GETTERS AND SETTER-----------------------------------------------
	
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
	

}

/*
 * (1)
 * Equation 3
 * Note that V_hot is the REMAINING temperature. 
 * 
 * 
 */