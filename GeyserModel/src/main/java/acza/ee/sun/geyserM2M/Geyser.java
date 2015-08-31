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
	private static final double c = 4184;//Specific heat capacity of water	[joule/(kg*Kelin)]
	private double R; 					//EWH thermal resistance [Kelvin/Watt]
	private double TANK_LENGTH; 		//Length of EWH in meters
	private double TANK_VOLUME; 		//Volume of EWH in liters
	private double TANK_RADIUS;			//Radius of tank in meters
	private double TANK_AREA;
	private double ENERGY_CAPACITY;
	
	//Geyser parameters
	private double t_inside;	//1-Node inside temperature 
	private double t_ambient;	//Ambient temperature outside EWH

	//Geyser variables
	private double t_inlet;
	private double t_outlet;
	private double energy_bottom_line;

	//-----------------------------------------------------------------------------------------------------------------------------------------
	
	
	public Geyser(double t_inside_initial){
		
		//Set EWH paramaters
		R = 0.4;
		TANK_LENGTH = 1; 		
		TANK_VOLUME = 100; 	
		TANK_RADIUS = Math.sqrt((TANK_VOLUME/1000)/(Math.PI*TANK_LENGTH));
		TANK_AREA = 2*Math.PI*TANK_RADIUS*TANK_LENGTH + 2*Math.PI*TANK_RADIUS*TANK_RADIUS;
					
		//Set initial EWH variable values. 
		t_inside = t_inside_initial;
		ENERGY_CAPACITY = waterEnthalpy(55, 0, TANK_VOLUME);
		energy_bottom_line = waterEnthalpy(t_inside_initial, 0, TANK_VOLUME);
	}
	
	//-----------------------------------------------------------------------------------------------------------------------------------------
	/**
	 * Steps the EWH state given a time in seconds
	 * @param step_seconds
	 * @return change in system energy:  (element gain) - (thermal loss)
	 */
	public double stepTime(long step_seconds, double added_power){
		
		//Calculate energy input by element and update INSIDE temperature
		double element_energy_added = added_power*step_seconds; 
		t_inside += deltaTemperature(element_energy_added, TANK_VOLUME);
		
		
		//Calculate change in INSIDE temperature and then thermal losses
		double t_inside_before = t_inside;
		t_inside = thermalDecay(step_seconds, t_inside_before, t_ambient, TANK_VOLUME, R);
		double termal_energy_loss = waterEnthalpy(t_inside_before, t_inside, TANK_VOLUME);
			
		
		energy_bottom_line += (element_energy_added - termal_energy_loss);
		
		return  element_energy_added - termal_energy_loss;
	}

	/**
	 * Steps the EWH state given a volume of hot water used
	 * @param usage_litres volume of hot water used
	 * @return energy_usage enthalpy of used water (energy that leaves system due to event)
	 */
	public double stepUsage(double usage_litres){
		
		//Update t_inside
		t_inside = ((TANK_VOLUME - usage_litres)/TANK_VOLUME) * (t_inside - t_inlet) + t_inlet; //(1)
		
		//Calculate the DELTA energy of the EWH (Energy loss due to usage + enery gain due to water input.
		// Think of this as a SWOP)
		double energy_usage = -waterEnthalpy(t_inside, t_inlet, usage_litres);
		energy_bottom_line += energy_usage;
		
		return energy_usage;
	}
	
	
	//----------------------------------------------------- Model equations -----------------------------------------------
	/**
	 * Calculates the energy in a volume of water due to its temperature and a reference temperature(pressure ignored).
	 * @param water_temperature
	 * @param ref_temperature temperature taken as ZERO energy reference
	 * @param liters_water
	 * @return energy in water volume relative to inlet temperature
	 */
	private double waterEnthalpy(double water_temperature, double ref_temperature, double liters_water){
		return c * rho *(liters_water * 0.001) * (water_temperature - ref_temperature);	//WHY DOES PHILIP NOT INCLUDE RHO?! (5) //(4)
	}
	/**
	 * Calculates the CHANGE in temperature of a volume of water due to adding/removing energy.
	 * @param energy energy added(+) or removed(-)
	 * @param liters_water volume of water
	 * @return delta T
	 */
	private double deltaTemperature(double energy, double liters_water){
		return energy/(c * rho * (liters_water * 0.001)); 
	}
	
	/**
	 * Calculates the NEW temperature of water after a exponential time decay.
	 * @param time in seconds
	 * @param t_before initial temperature
	 * @param t_ambient ambient temperature
	 * @param volume in liters
	 * @param thermal_resistance
	 * @return
	 */
	private double thermalDecay(double time, double t_inital, double t_ambient, double volume, double thermal_resistance){
		return t_ambient + (t_inital - t_ambient)*Math.exp((-1.0 * time)/(c*rho*(0.001*volume)*thermal_resistance)); //(3)
	}
	
	
	//----------------------------------------------------- GETTERS AND SETTER-----------------------------------------------
	public double getInsideTemperature() {
		return this.t_inside;
	}

	public double getInletTemperature(){
		return this.t_inlet;
	}
	
	public double getAmbientTemperature(){
		return this.t_ambient;
	}
	
	public double getEnergyContent(){
		return this.energy_bottom_line;
	}
	
	public void setInletTemperature(double t_inlet){
		this.t_inlet = t_inlet;
	}
	
	public void setAmbientTemperature(double t_ambient){
		this.t_ambient = t_ambient;
	}
	
	public String toString(){
		return  "***************************\n"
				+ "t_inside: \t" + String.format("%.2f",t_inside) + "\n";
	}

	public String toJSON(){
		return "{"
				+"\"t_inside\":" + String.format("%.2f",t_inside)+ ", "
				+ "}";
	}

	public String toCSV(){
		double t_out = (2.7*Math.pow(10,-4))*Math.pow((t_inside-60), 3) + 60;
		return String.format("%.2f,%.2f,%.2f,%.2f,%.2f", t_inside,t_out,t_inlet,t_ambient,energy_bottom_line/1000000);
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
 * (3) (FIXED. Philip used weird units, so ignore this.)
 * Equation 9. Remember R = Watt/(Kg*day) therefore time has to be expressed in DAYS. i.e. SECONDS/(60*60*24)
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