/* --------------------------------------------------------------------------------------------------------
 * DATE:	15 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Datapoint representing a single entry of a set of measured state variables of an EWH.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DataStamp {

	public final static String CSV_HEADER = "ewh_id,server_stamp,client_stamp,t1,t2,t3,t4,drip_detect,valve_state,relay_state,"
			+ "watt_avgpmin,kwatt_tot,hot_flow_ratepmin,hot_litres_tot,cold_flow_ratepmin,cold_litres_tot,hot_dif,cold_dif,kwatt_dif";
	//TIE THIS TO THE VARIABLE NAMES: DUPLICATE CODE
	
	public final String ewh_id;
	public final long server_stamp;
	public final long client_stamp;
	public final double t_outlet;
	public final double t_far;
	public final double t_inlet;
	public final double t_ambient;
	public final boolean drip_detect;
	public final boolean valve_state;
	public final boolean relay_state;
	public final double watt_avgpmin;
	public final double kwatt_tot;
	public final double hot_flow_ratepmin;
	public final double hot_litres_tot;
	public final double cold_flow_ratepmin;
	public final double cold_litres_tot;
	public double hot_dif;
	public double cold_dif;
	public double kwatt_dif;
	
	
	
	/**
	 * 
	 * @param timestamp UNIX timestamp in seconds
	 * @param usage_litres
	 */
	public DataStamp(	String ewh_id,
						long server_stamp, long client_stamp, 
						double t_outlet, double t_far, double t_inlet, double t_ambient,
						boolean drip_detect, boolean valve_state, boolean relay_state, 
						double watt_avgpmin, double kwatt_tot,
						double hot_flow_ratepmin, double hot_litres_tot, double cold_flow_ratepmin, double cold_litres_tot){
		
		this.ewh_id = ewh_id;
		this.server_stamp = server_stamp;
		this.client_stamp = client_stamp;
		this.t_outlet = t_outlet;
		this.t_far = t_far;
		this.t_inlet = t_inlet;
		this.t_ambient = t_ambient;
		this.drip_detect = drip_detect;
		this.valve_state = valve_state;
		this.relay_state = relay_state;
		this.watt_avgpmin = watt_avgpmin;
		this.kwatt_tot = kwatt_tot;
		this.hot_flow_ratepmin = hot_flow_ratepmin;
		this.hot_litres_tot = hot_litres_tot;
		this.cold_flow_ratepmin = cold_flow_ratepmin;
		this.cold_litres_tot = cold_litres_tot;
		this.hot_dif = 0;
		this.cold_dif = 0;
		this.kwatt_dif = 0;
	}
	

	public String toString(){
		return "" 	+ ewh_id + "," 
					+ timestampToString(server_stamp) + "," + timestampToString(client_stamp)  + "," 
					+ t_outlet + "," +  t_far + "," + t_inlet  + "," + t_ambient  + ","
					+ drip_detect + "," + valve_state  + "," + relay_state + ","
					+ watt_avgpmin + "," + kwatt_tot + ","
					+ hot_flow_ratepmin + "," + hot_litres_tot + "," + cold_flow_ratepmin + "," + cold_litres_tot  + "," 
					+ hot_dif + "," + cold_dif + "," + kwatt_dif;
	}
	
    private static String timestampToString(long stamp){
    	DateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    	return df.format(new Date(stamp));
    }
}