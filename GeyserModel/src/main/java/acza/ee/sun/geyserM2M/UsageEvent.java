package acza.ee.sun.geyserM2M;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/***
 * 
 * @author andrew
 * 
 * The UsageEvent it not only important to analyse and interperet the raw data, but also
 * offers an abstraction to INPUT data for a EWH model simulation
 */


public class UsageEvent {

	public final static String CSV_HEADER = "start_time,volume,duration,mean_flowrate,mean_temp_out,mean_temp_in,enthalpy_kwh";
	
	public long start_time;
	public double volume;
	public long duration;
	public double mean_flowrate;
	public double mean_temp_out;
	public double mean_temp_in;
	public double enthalpy_kwh;
	
	public UsageEvent(long start_time, double volume, long duration, double mean_temp_out, double mean_temp_in){
		
		this.start_time = start_time;
		this.volume = volume;
		this.duration = duration;
		this.mean_temp_out = mean_temp_out;
		this.mean_temp_in = mean_temp_in;
		this.enthalpy_kwh = Geyser.waterEnthalpy(mean_temp_out, mean_temp_in, volume)/(1000*3600);
		
		if(duration != 0.0)
			this.mean_flowrate = volume/duration;
		else
			this.mean_flowrate = 0;
	}
	
	public String toString(){
		return "" 	+ timestampToString(start_time) + "," + volume + "," 
					+ duration + "," +  mean_flowrate + "," + mean_temp_out  + "," + mean_temp_in + "," 
					+ enthalpy_kwh;
	}
	
    private static String timestampToString(long stamp){
    	DateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    	return df.format(new Date(stamp));
    }
	
}
