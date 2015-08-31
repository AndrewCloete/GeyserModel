/* --------------------------------------------------------------------------------------------------------
 * DATE:	15 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Datapoint representing a single entry of a set of state variables of an EWH.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;

import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	
	public final long timestamp;
	public final double usage_litres;
	public final double power;
	public final double t_outlet;
	public final double t_far;
	public final double t_inlet;
	public final double t_ambient;
	
	
	/**
	 * 
	 * @param timestamp UNIX timestamp in seconds
	 * @param usage_litres
	 */
	public DataPoint(long timestamp, double usage_litres, double power, double t_outlet, double t_far, double t_inlet, double t_ambient){
		this.timestamp = timestamp;
		this.usage_litres = usage_litres;
		this.power = power;
		this.t_outlet = t_outlet;
		this.t_far = t_far;
		this.t_inlet = t_inlet;
		this.t_ambient = t_ambient;
	}
	
	/**
	 * 
	 * @param filename: JSON file containing list of UNIX timestamped usage points (seconds and litres).
	 * @return Populated list of UsagePoints
	 */
	public static LinkedList<DataPoint> importUsageFromJSONFile(String filepath){
		
		LinkedList<DataPoint> data_points = new LinkedList<DataPoint>();
		
		try {
			//Read file and decode JSON object.
			byte[] encoded = Files.readAllBytes(Paths.get(filepath));
			String json_object_str = new String(encoded, StandardCharsets.UTF_8);
			JSONObject json_object = new JSONObject(json_object_str);
			JSONArray json_usage_dataset = (JSONArray) json_object.get("dataset");
			logger.info("Read file with GeyserID: " + json_object.get("geyser_id") + " and dataset length: " + json_usage_dataset.length());
			
			//Traverse all data entries in JSON object and populate usage list
			SimpleDateFormat sdf  = new SimpleDateFormat("yy/MM/dd kk:mm");
			for(int i = 0; i < json_usage_dataset.length(); i++){
				JSONObject json_datapoint = (JSONObject) json_usage_dataset.get(i);
				try {
					Date date = sdf.parse((String)json_datapoint.get("server_stamp"));
					double usage = (Double)json_datapoint.get("hot_flow_ratepmin");
					double wattage = (Double)json_datapoint.get("watt_avgpmin");
					double t_inside = (Integer)json_datapoint.get("t1");
					double t_far = (Integer)json_datapoint.get("t2");
					double t_inlet = (Integer)json_datapoint.get("t3");
					double t_ambient = (Integer)json_datapoint.get("t4");
					
					data_points.add(new DataPoint(date.getTime()/1000L, usage, wattage, t_inside, t_far, t_inlet, t_ambient));
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
	
	public static LinkedList<DataPoint> importUsageFromDatabase(){
		LinkedList<DataPoint> data_points = new LinkedList<DataPoint>();
		
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		
		String query = "SELECT * from timestamps where geyser_id=110  AND client_stamp >= '2015-08-29 00:00:00' AND client_stamp <= '2015-08-30 00:00:00'";
		
		Connection rdb_conn = null;
		Statement stmt = null;
		try{
			//Register JDBC driver
			Class.forName(JDBC_DRIVER);

			//Open a connection
			rdb_conn = DriverManager.getConnection("jdbc:mysql://146.232.128.163/GeyserM2M","intelligeyser","ewhM2Mnscl");

			//Execute a query
			stmt = rdb_conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				long server_stamp = rs.getTimestamp("server_stamp").getTime();
				double usage = rs.getFloat("hot_flow_ratepmin");
				double wattage = rs.getFloat("watt_avgpmin");
				double t_inside = rs.getInt("t1");
				double t_far = rs.getInt("t2");
				double t_inlet = rs.getInt("t3");
				double t_ambient = rs.getInt("t4");
				
				data_points.add(new DataPoint(server_stamp/1000L, usage, wattage, t_inside, t_far, t_inlet, t_ambient));
			}
			
			logger.info("Successfully read database");
			
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

	
}
