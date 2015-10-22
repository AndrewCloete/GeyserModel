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

import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.sql.*;

public class DataPoint {
	
	private static final Logger logger = LogManager.getLogger(DataPoint.class);
	
	public final static String CSV_HEADER = "server_stamp,client_stamp,t1,t2,t3,t4,drip_detect,valve_state,relay_state,"
			+ "watt_avgpmin,kwatt_tot,hot_flow_ratepmin,hot_litres_tot,cold_flow_ratepmin,cold_litres_tot";
	
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
	
	
	
	/**
	 * 
	 * @param timestamp UNIX timestamp in seconds
	 * @param usage_litres
	 */
	public DataPoint(	long server_stamp, long client_stamp, 
						double t_outlet, double t_far, double t_inlet, double t_ambient,
						boolean drip_detect, boolean valve_state, boolean relay_state, 
						double watt_avgpmin, double kwatt_tot,
						double hot_flow_ratepmin, double hot_litres_tot, double cold_flow_ratepmin, double cold_litres_tot){
		
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
	}
	
	/**
	 * 
	 * @param filename: JSON file containing list of UNIX timestamped usage points (seconds and litres).
	 * @return Populated list of UsagePoints
	 */
	public static LinkedList<DataPoint> importDataFromJSONFile(String filepath){
		
		LinkedList<DataPoint> data_points = new LinkedList<DataPoint>();
		
		try {
			//Read file and decode JSON object.
			byte[] encoded = Files.readAllBytes(Paths.get(filepath.trim()));
			String json_object_str = new String(encoded, StandardCharsets.UTF_8);
			JSONObject json_object = new JSONObject(json_object_str);
			JSONArray json_usage_dataset = (JSONArray) json_object.get("dataset");
			logger.info("Read file with GeyserID: " + json_object.get("geyser_id") + " and dataset length: " + json_usage_dataset.length());
			
			//Traverse all data entries in JSON object and populate usage list
			SimpleDateFormat sdf  = new SimpleDateFormat("yy-MM-dd kk:mm:ss"); //15-09-01 18:34:53
			for(int i = 0; i < json_usage_dataset.length(); i++){
				JSONObject json_datapoint = (JSONObject) json_usage_dataset.get(i);
				try {
					Date server_stamp = sdf.parse((String)json_datapoint.get("server_stamp"));
					Date client_stamp = sdf.parse((String)json_datapoint.get("client_stamp"));
					double t_outlet = (Double)json_datapoint.get("t1");
					double t_far = (Double)json_datapoint.get("t2");
					double t_inlet = (Double)json_datapoint.get("t3");
					double t_ambient = (Double)json_datapoint.get("t4");
					boolean drip_detect = (Boolean)json_datapoint.get("drip_detect");
					boolean valve_state = (Boolean)json_datapoint.get("valve_state");
					boolean relay_state = (Boolean)json_datapoint.get("relay_state");
					double watt_avgpmin = (Double)json_datapoint.get("watt_avgpmin");
					double kwatt_tot = (Double)json_datapoint.get("kwatt_tot");
					double hot_flow_ratepmin = (Double)json_datapoint.get("hot_flow_ratepmin");
					double hot_litres_tot = (Double)json_datapoint.get("hot_litres_tot");
					double cold_flow_ratepmin = (Double)json_datapoint.get("cold_flow_ratepmin");
					double cold_litres_tot = (Double)json_datapoint.get("cold_litres_tot");
					
					
					data_points.add(new DataPoint(server_stamp.getTime()/1000L, client_stamp.getTime()/1000L, 
									t_outlet, t_far, t_inlet, t_ambient,
									drip_detect, valve_state, relay_state, 
									watt_avgpmin, kwatt_tot, 
									hot_flow_ratepmin, hot_litres_tot, cold_flow_ratepmin, cold_litres_tot));
				} catch (ParseException e) {
					logger.error("Unable to parse JSON.",e);
				}
			}
			
		} catch (IOException e) {
			logger.error("Unable to read/locate JSON file", e);
		} catch (JSONException e){
			logger.error("Corrupt JSON", e);
		}
		
		return data_points;
	}
	
	/***
	 * 
	 * @param db_url: jdbc:mysql://<database_server_ip>/<database_name>. e.g. jdbc:mysql://146.232.128.163/GeyserM2M
	 * @param db_user
	 * @param db_psk
	 * @param geyser_id
	 * @param start_timestamp
	 * @param end_timestamp
	 * @return
	 */
	public static LinkedList<DataPoint> importDataFromDatabase(String db_url, String db_user, String db_psk, int geyser_id, Date start_ts, Date end_ts){
		LinkedList<DataPoint> data_points = new LinkedList<DataPoint>();
		
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String query = "SELECT * from timestamps where geyser_id="
				+ geyser_id
				+ " AND client_stamp >= "
				+ "'" + df.format(start_ts) + "'"
				+ "AND client_stamp <= "
				+ "'" + df.format(end_ts) + "'";
		
		Connection rdb_conn = null;
		Statement stmt = null;
		try{
			//Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//Open a connection
			rdb_conn = DriverManager.getConnection(db_url,db_user,db_psk);

			//Execute a query
			stmt = rdb_conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				long server_stamp = rs.getTimestamp("server_stamp").getTime();
				long client_stamp = rs.getTimestamp("client_stamp").getTime();
				double t_outlet = rs.getInt("t1");
				double t_far = rs.getInt("t2");
				double t_inlet = rs.getInt("t3");
				double t_ambient = rs.getInt("t4");
				boolean drip_detect = rs.getBoolean("drip_detect");
				boolean valve_state = rs.getBoolean("valve_state");
				boolean relay_state = rs.getBoolean("relay_state");
				double watt_avgpmin = rs.getFloat("watt_avgpmin");
				double kwatt_tot = rs.getFloat("kwatt_tot");
				double hot_flow_ratepmin = rs.getFloat("hot_flow_ratepmin");
				double hot_litres_tot = rs.getFloat("hot_litres_tot");
				double cold_flow_ratepmin = rs.getFloat("cold_flow_ratepmin");
				double cold_litres_tot = rs.getFloat("cold_litres_tot");
				
				
				data_points.add(new DataPoint(server_stamp/1000L, client_stamp/1000L, 
						t_outlet, t_far, t_inlet, t_ambient,
						drip_detect, valve_state, relay_state, 
						watt_avgpmin, kwatt_tot, 
						hot_flow_ratepmin, hot_litres_tot, cold_flow_ratepmin, cold_litres_tot));
			}
			
			logger.info("Successfully read database with query: " + query);
			
			stmt.close();
			rdb_conn.close();
		}catch(SQLException se){
			//Handle errors for JDBC
			logger.error("SQLException: ", se);
		}catch(Exception e){
			//Handle errors for Class.forName
			logger.error("Unexpected database exception: ", e);
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					stmt.close();
			}catch(SQLException se2){
				logger.error("SQLException closing statement: ", se2);
			}// nothing we can do
			try{
				if(rdb_conn!=null)
					rdb_conn.close();
			}catch(SQLException se){
				logger.error("SQLException closing database connection: ", se);
			}
		}
		
		
		return data_points;
	}

	public String toString(){
		return "" 	+ timestampToString(server_stamp*1000) + "," + timestampToString(client_stamp*1000)  + "," 
					+ t_outlet + "," +  t_far + "," + t_inlet  + "," + t_ambient  + ","
					+ drip_detect + "," + valve_state  + "," + relay_state + ","
					+ watt_avgpmin + "," + kwatt_tot + ","
					+ hot_flow_ratepmin + "," + hot_litres_tot + "," + cold_flow_ratepmin + "," + cold_litres_tot;
	}
	
    private static String timestampToString(long stamp){
    	DateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    	return df.format(new Date(stamp));
    }
}