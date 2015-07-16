/* --------------------------------------------------------------------------------------------------------
 * DATE:	15 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Example implementation of Geyser Model.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;

import java.io.IOException;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class UsagePoint {
	
	private static final Logger logger = LogManager.getLogger(UsagePoint.class);
	
	public final long timestamp;
	public final double usage_litres;
	
	/**
	 * 
	 * @param timestamp UNIX timestamp in seconds
	 * @param usage_litres
	 */
	public UsagePoint(long timestamp, double usage_litres){
		this.timestamp = timestamp;
		this.usage_litres = usage_litres;
	}
	
	/**
	 * 
	 * @param filename: JSON file containing list of UNIX timestamped usage points (seconds and litres).
	 * @return Populated list of UsagePoints
	 */
	public static LinkedList<UsagePoint> importUsageFromJSONFile(String filepath){
		
		LinkedList<UsagePoint> usage_points = new LinkedList<UsagePoint>();
		
		try {
			//Read file and decode JSON object.
			byte[] encoded = Files.readAllBytes(Paths.get(filepath));
			String json_object_str = new String(encoded, StandardCharsets.UTF_8);
			JSONObject json_object = new JSONObject(json_object_str);
			JSONArray json_usage_dataset = (JSONArray) json_object.get("usage_dataset");
			logger.info("Read file with GeyserID: " + json_object.get("geyser_id") + " and dataset length: " + json_usage_dataset.length());
			
			//Traverse all data entries in JSON object and populate usage list
			for(int i = 0; i < json_usage_dataset.length(); i++){
				JSONObject json_datapoint = (JSONObject) json_usage_dataset.get(i);
				usage_points.add(new UsagePoint(((Integer)json_datapoint.get("ts")), (Double)json_datapoint.get("litres")));
			}
			
		} catch (IOException e) {
			logger.error("Unable to read/locate JSON file", e);
		} catch (JSONException e){
			logger.error("Corrupt JSON", e);
		}
		
		return usage_points;
	}
	
	//TODO: public static LinkedList<UsagePoint> importUsageFromDatabase(DATABASE)

	
}
