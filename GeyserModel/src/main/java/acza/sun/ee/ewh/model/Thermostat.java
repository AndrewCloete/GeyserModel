/* --------------------------------------------------------------------------------------------------------
 * DATE:	16 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: A geyser thermostat
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.sun.ee.ewh.model;

public class Thermostat {
	
	private double setpiont;
	private double deadband;
	private boolean element_state;
	
	public Thermostat(double setpiont, double deadband){
		this.setpiont = setpiont;
		this.deadband = deadband;
		this.element_state = false;
	}

	
	public boolean elementState(double temperature){
		if(temperature >= setpiont + deadband/2)
			return false;
		else if(temperature <= setpiont - deadband/2)
			return true;
		else
			return this.element_state;
	}
	
	//----------------------------------------------------- GETTERS AND SETTER-----------------------------------------------
	public double getSetpiont() {
		return setpiont;
	}

	public void setSetpiont(double setpiont) {
		this.setpiont = setpiont;
	}

	public double getDeadband() {
		return deadband;
	}

	public void setDeadband(double deadband) {
		this.deadband = deadband;
	}
	
	

}

