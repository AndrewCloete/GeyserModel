/* --------------------------------------------------------------------------------------------------------
 * DATE:	13 Jul 2015
 * AUTHOR:	Cloete A.H
 * PROJECT:	M-Eng, Inteligent geyser M2M system.	
 * ---------------------------------------------------------------------------------------------------------
 * DESCRIPTION: Example implementation of Geyser Model.
 * ---------------------------------------------------------------------------------------------------------
 * PURPOSE: 
 * ---------------------------------------------------------------------------------------------------------
 */

package acza.ee.sun.geyserM2M;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Properties;


public class GeyserSimulator 
{	
	private static final Logger logger = LogManager.getLogger(GeyserSimulator.class);
	private static PrintWriter writer;
	
	private static String DB_URL;
	private static String DB_USER;
	private static String DB_PSK;
	private static int GEYSER_ID;
	private static Date START_TIMESTAMP;
	private static Date END_TIMESTAMP;
	private static String JSON_INPUT_FILEPATH;
	private static String OUTPUT_FOLDERPATH;
	
	
    public static void main( String[] args )
    {
    	// ---------------------- Sanity checking of command line arguments ---------------------------------------------
		if( (args.length != 1) || !(args[0].equalsIgnoreCase("-d")||args[0].equalsIgnoreCase("-f")))
		{
			System.out.println( "Usage: "
					+ "\n\t-f \t Use *.json input file) "
					+ "\n\t-d \t Directly input from database"
					+ "\n"
					+ "Configuration is specified in config.properties") ;
			return;
		}
		
		String input_selection_flag = args[0];
    	//---------------------------------------------------------------------------------------------------------------
    	
    	// ---------------------- Reading and sanity checking configuration parameters -------------------------------------------
    	Properties configFile = new Properties();
    	try {
    		configFile.load(GeyserSimulator.class.getClassLoader().getResourceAsStream("config.properties"));
    		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		
    		DB_URL = configFile.getProperty("DB_URL");
    		DB_USER = configFile.getProperty("DB_USER");
			DB_PSK = configFile.getProperty("DB_PSK");
			GEYSER_ID = new Integer(configFile.getProperty("GEYSER_ID"));
			START_TIMESTAMP = sdf.parse(configFile.getProperty("START_TIMESTAMP"));
			END_TIMESTAMP = sdf.parse(configFile.getProperty("END_TIMESTAMP"));
			JSON_INPUT_FILEPATH = configFile.getProperty("JSON_INPUT_FILEPATH");
			OUTPUT_FOLDERPATH = configFile.getProperty("OUTPUT_FOLDERPATH");

    	} catch (IOException e) {
    		logger.fatal("Error in interpereting configuration file \"config.properties\"", e);
    		return;
    	} catch (ParseException e) {
    		logger.fatal("Incorrect date format in \"config.properties\". Use yyyy/MM/dd HH:mm:ss", e);
    		return;
		}
    	//-------------------------------------------------------------------------------------------------------
    	logger.info("Geyser simulator started with parameters: " + configFile.toString());
        
    	
    	try {
    		if(input_selection_flag.equalsIgnoreCase("-f")){
    			
    			String filename = JSON_INPUT_FILEPATH.substring(JSON_INPUT_FILEPATH.lastIndexOf("/")+1);
    			filename = filename.substring(0,filename.lastIndexOf("."));
    			writer = new PrintWriter(OUTPUT_FOLDERPATH + "result_" + filename + ".csv", "UTF-8");
    		}
    		else if(input_selection_flag.equalsIgnoreCase("-d")){
    			writer = new PrintWriter(OUTPUT_FOLDERPATH + "result_" + GEYSER_ID + ".csv", "UTF-8");
    		}
    		
		} catch (FileNotFoundException e) {
			logger.error("Output file not found: ", e);
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return;
		}
    	
    	
    	//Import usage points.
    	LinkedList<DataPoint> data_points = null;
    	if(input_selection_flag.equalsIgnoreCase("-f")) 
    		data_points = DataPoint.importUsageFromJSONFile(JSON_INPUT_FILEPATH);
    	else if(input_selection_flag.equalsIgnoreCase("-d"))
    		data_points = DataPoint.importUsageFromDatabase(DB_URL, DB_USER, DB_PSK, GEYSER_ID, START_TIMESTAMP, END_TIMESTAMP);
    	
    	//Create iterator.
    	ListIterator<DataPoint> data_iterator = data_points.listIterator();
    	
    	//Read first datapoint
    	DataPoint point = data_iterator.next();
    	
    	//Create and initialise new Geyser object
    	Geyser ewh = new Geyser(0.4, point.t_outlet);
    	
    	//Initialise CSV output file header
    	writer.println(DataPoint.CSV_HEADER + "," + Geyser.CSV_HEADER);
    	
    	//Iterate through usage points and step simulation
    	while(data_iterator.hasNext()){
    	    
    		DataPoint next_point = data_iterator.next();	
    		ewh.setInletTemperature(point.t_inlet);
    		ewh.setAmbientTemperature(point.t_ambient);
    		ewh.stepUsage(point.hot_flow_ratepmin);
    		ewh.stepTime(next_point.server_stamp - point.server_stamp, point.watt_avgpmin*1000);
    		writer.println(point + "," + ewh.toCSV());
    		
    		point = next_point;
    	}
    	writer.close();
    	logger.info("Geyser simulator finished");
    }


    
}



