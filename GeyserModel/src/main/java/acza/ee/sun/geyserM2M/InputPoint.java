/* --------------------------------------------------------------------------------------------------------
 * DATE:	31 Aug 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Point representing a single entry of a set of input variables to an EWH.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InputPoint {

	private static final Logger logger = LogManager.getLogger(InputPoint.class);

	public final static String CSV_HEADER = "timestamp, t_inlet, t_ambient, water_usage, power_usage";

	public final long timestamp;
	public final double t_inlet;
	public final double t_ambient;
	public final double water_usage;
	public final double power_usage;


	public InputPoint(long timestamp, double t_inlet, double t_ambient, double water_usage, double power_usage){

		this.timestamp = timestamp;
		this.t_inlet = t_inlet;
		this.t_ambient = t_ambient;
		this.water_usage = water_usage;
		this.power_usage = power_usage;
	}

	public static LinkedList<InputPoint> importInputFromJSONFile(String filepath){
		LinkedList<InputPoint> input_points = new LinkedList<InputPoint>();

		try {
			//Read file and decode JSON object.
			byte[] encoded = Files.readAllBytes(Paths.get(filepath.trim()));
			String json_object_str = new String(encoded, StandardCharsets.UTF_8);
			JSONObject json_object = new JSONObject(json_object_str);
			JSONArray json_usage_dataset = (JSONArray) json_object.get("dataset");
			logger.info("Read file with GeyserID: " + json_object.get("geyser_id") + " and dataset length: " + json_usage_dataset.length());

			//Traverse all data entries in JSON object and populate usage list
			SimpleDateFormat sdf  = new SimpleDateFormat("yy/MM/dd kk:mm");
			for(int i = 0; i < json_usage_dataset.length(); i++){
				JSONObject json_datapoint = (JSONObject) json_usage_dataset.get(i);
				try {
					Date timestamp = sdf.parse((String)json_datapoint.get("server_stamp"));
					double t_inlet = (Integer)json_datapoint.get("t3");
					double t_ambient = (Integer)json_datapoint.get("t4");			
					double water_usage = (Double)json_datapoint.get("hot_flow_ratepmin");
					double power_usage = (Double)json_datapoint.get("watt_avgpmin");

					input_points.add(new InputPoint(timestamp.getTime()/1000L, t_inlet, t_ambient, water_usage, power_usage));
				} catch (ParseException e) {
					logger.error("Unable to parse JSON.",e);
				}
			}

		} catch (IOException e) {
			logger.error("Unable to read/locate JSON file", e);
		} catch (JSONException e){
			logger.error("Corrupt JSON", e);
		}

		return input_points;
	}

	public static LinkedList<InputPoint> importInputFromDatabase(String db_url, String db_user, String db_psk, int geyser_id, Date start_ts, Date end_ts){
		LinkedList<InputPoint> input_points = new LinkedList<InputPoint>();

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
				long timestamp = rs.getTimestamp("server_stamp").getTime();
				double t_inlet = rs.getInt("t3");
				double t_ambient = rs.getInt("t4");
				double water_usage = rs.getFloat("hot_flow_ratepmin");
				double power_usage = rs.getFloat("watt_avgpmin");

				input_points.add(new InputPoint(timestamp/1000L, t_inlet, t_ambient, water_usage, power_usage));
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


		return input_points;
	}
	
	public static LinkedList<InputPoint> importFromDataPoints(LinkedList<DataPoint> data_points){
		LinkedList<InputPoint> input_points = new LinkedList<InputPoint>();
		
		//Create iterator.
    	ListIterator<DataPoint> data_iterator = data_points.listIterator();
    	
    	while(data_iterator.hasNext()){
    		DataPoint point = data_iterator.next();
    		input_points.add(new InputPoint(point.server_stamp, point.t_inlet, point.t_ambient, point.hot_flow_ratepmin, point.watt_avgpmin));
    	}
		
		return input_points;
	}

	public String toString(){
		return "" 	+ timestampToString(timestamp*1000) + "," 
				+ t_inlet  + "," + t_ambient  + ","
				+ water_usage + ","
				+ power_usage;
	}

	private static String timestampToString(long stamp){
		DateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		return df.format(new Date(stamp));
	}
}
