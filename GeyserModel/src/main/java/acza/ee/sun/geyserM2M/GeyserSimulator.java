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
	private static PrintWriter results_writer;
	private static PrintWriter data_writer;
	
	private static String DB_URL;
	private static String DB_USER;
	private static String DB_PSK;
	private static int GEYSER_ID;
	private static Date START_TIMESTAMP;
	private static Date END_TIMESTAMP;
	private static String JSON_INPUT_FILEPATH;
	private static String OUTPUT_FOLDERPATH;
	
	private static LinkedList<DataPoint> data_points = null;
	private static LinkedList<InputPoint> input_points = null;
	
	
    public static void main( String[] args )
    {
    	// ---------------------- Sanity checking of command line arguments ---------------------------------------------
		if( (args.length != 2)  || !(args[0].equalsIgnoreCase("-d")||args[0].equalsIgnoreCase("-f"))
								|| !(args[1].equalsIgnoreCase("-v")||args[1].equalsIgnoreCase("-p")))
		{
			System.out.println( "Usage: "
					+ "\nSelection 1 - INPUT SOURCE: \t-f : Use *.json input file) \t-d : Directly input from database"
					+ "\nSelection 2 - SIMULATION TYPE \t-v : Verification mode \t-p : Predictive mode"
					+ "\n"
					+ "Configuration is specified in config.properties") ;
			return;
		}
		
		String input_selection_flag = args[0];
		String simtype_selection_flag = args[1];
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
        
    	
    	//Create output file, import input data based in parameter selection flag and initialise CSV output file header
    	try {
    		if(input_selection_flag.equalsIgnoreCase("-f")){
    			
    			String filename = JSON_INPUT_FILEPATH.substring(JSON_INPUT_FILEPATH.lastIndexOf("/")+1);
    			filename = filename.substring(0,filename.lastIndexOf("."));
    			results_writer = new PrintWriter(OUTPUT_FOLDERPATH + "result_" + filename + ".csv", "UTF-8");
    			data_writer = new PrintWriter(OUTPUT_FOLDERPATH + "data_" + filename + ".csv", "UTF-8");
    			
    			if(simtype_selection_flag.equalsIgnoreCase("-v")){
    				data_points = DataPoint.importDataFromJSONFile(JSON_INPUT_FILEPATH);
    				input_points = InputPoint.importFromDataPoints(data_points);
    				data_writer.println(DataPoint.CSV_HEADER); //Initialise data CSV output file header
    			}
				else if(simtype_selection_flag.equalsIgnoreCase("-p")){
					input_points = InputPoint.importInputFromJSONFile(JSON_INPUT_FILEPATH);
				}
    		}
    		else if(input_selection_flag.equalsIgnoreCase("-d")){
    			results_writer = new PrintWriter(OUTPUT_FOLDERPATH + "result_" + GEYSER_ID + ".csv", "UTF-8");
    			data_writer = new PrintWriter(OUTPUT_FOLDERPATH + "data_" + GEYSER_ID + ".csv", "UTF-8");
    			
    			if(simtype_selection_flag.equalsIgnoreCase("-v")){
    				data_points = DataPoint.importDataFromDatabase(DB_URL, DB_USER, DB_PSK, GEYSER_ID, START_TIMESTAMP, END_TIMESTAMP);
    				input_points = InputPoint.importFromDataPoints(data_points);
    				data_writer.println(DataPoint.CSV_HEADER); //Initialise data CSV output file header
    			}
				else if(simtype_selection_flag.equalsIgnoreCase("-p")){
					input_points = InputPoint.importInputFromDatabase(DB_URL, DB_USER, DB_PSK, GEYSER_ID, START_TIMESTAMP, END_TIMESTAMP);
				}
    		}
    		
    		//Initialise results CSV output file header
    		results_writer.println(InputPoint.CSV_HEADER + "," + Geyser.CSV_HEADER);
    		
		} catch (FileNotFoundException e) {
			logger.error("Output file not found: ", e);
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return;
		}
    	
    	
    	    	
    	//Create iterators.
    	ListIterator<InputPoint> input_iterator = input_points.listIterator();
    	
    	//Read first datapoint
    	InputPoint point = input_iterator.next();
    	
    	//Create and initialise new Geyser object
    	Geyser ewh;
    	try{
    		double thermal_resistance = new Double(configFile.getProperty("THERMAL_RESISTANCE").trim());
    		double tank_volume = new Double(configFile.getProperty("TANK_VOLUME").trim());
    		
    		//Determine initial temperature of EWH from simulation selection and config.properties. 
    		double initial_temperature = 0; 
    		String it_str = configFile.getProperty("START_TEMPERATURE").trim();
    		if(simtype_selection_flag.equalsIgnoreCase("-v")){
	    		if(it_str.equalsIgnoreCase("DEFAULT"))
	    			initial_temperature = data_points.peek().t_outlet;
	    		else if(it_str.contains("+"))
	    			initial_temperature = data_points.peek().t_outlet + new Double(it_str.substring(it_str.lastIndexOf("+")+1));
	    		else if(it_str.contains("-"))
	    			initial_temperature = data_points.peek().t_outlet - new Double(it_str.substring(it_str.lastIndexOf("-")+1));
	    		else
	    			initial_temperature = new Double(it_str);
    		}
    		else if(simtype_selection_flag.equalsIgnoreCase("-p")){
				if(it_str.equalsIgnoreCase("DEFAULT"))
					initial_temperature = 50;
				else if(it_str.contains("+"))
					initial_temperature = 50 + new Double(it_str.substring(it_str.lastIndexOf("+")+1));
				else if(it_str.contains("-"))
					initial_temperature = 50 - new Double(it_str.substring(it_str.lastIndexOf("-")+1));
				else
					initial_temperature = new Double(it_str);
    		}
    		
    		//Instantiate geyser object
    		ewh = new Geyser(thermal_resistance, tank_volume, initial_temperature);
    	}catch(NumberFormatException e){
    		logger.fatal("Incorrect EWH model parameters format in config.properties", e);
    		return;
    	}	
    	
    	//Iterate through usage points and step simulation
    	while(input_iterator.hasNext()){
    	    
    		InputPoint next_point = input_iterator.next();	
    		ewh.setInletTemperature(point.t_inlet);
    		ewh.setAmbientTemperature(point.t_ambient);
    		ewh.stepUsage(point.water_usage);
    		ewh.stepTime(next_point.timestamp - point.timestamp, point.power_usage*1000);
    		results_writer.println(point + "," + ewh.toCSV());
    		
    		point = next_point;
    	}
    	
    	//If verification mode is selected, print real data to file
    	ListIterator<DataPoint> data_iterator;
    	if(simtype_selection_flag.equalsIgnoreCase("-v")){
    		data_iterator = data_points.listIterator();
    		
    		while(data_iterator.hasNext()){
    			data_writer.println(data_iterator.next());
    		}
    	}
    	
    	results_writer.close();
    	data_writer.close();
    	logger.info("Geyser simulator finished");
    }


    
}


