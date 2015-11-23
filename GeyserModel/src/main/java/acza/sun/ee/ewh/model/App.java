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

package acza.sun.ee.ewh.model;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import acza.sun.ee.ewh.data.DataSegment;
import acza.sun.ee.ewh.data.DataStamp;
import acza.sun.ee.ewh.data.DataSummary;
import acza.sun.ee.ewh.data.EWHSQLDatabase;
import acza.sun.ee.ewh.data.UsageEvent;

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


public class App 
{	
	private static final Logger logger = LogManager.getLogger(App.class);
	private static PrintWriter results_writer;
	
	private static String DB_URL;
	private static String DB_USER;
	private static String DB_PSK;
	private static String GEYSER_ID;
	private static Date START_TIMESTAMP;
	private static Date END_TIMESTAMP;
	private static String OUTPUT_FILEPATH;	
	
	//private long sim_clock; //Virtual time of simulation environment
	
	
    public static void main( String[] args )
    {
    	// ---------------------- Sanity checking of command line arguments ---------------------------------------------
    		//None for now
    	//---------------------------------------------------------------------------------------------------------------
    	
    	// ---------------------- Reading and sanity checking configuration parameters -------------------------------------------
    	Properties configFile = new Properties();
    	try {
    		configFile.load(App.class.getClassLoader().getResourceAsStream("config.properties"));
    		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		
    		DB_URL = configFile.getProperty("DB_URL").trim();
    		DB_USER = configFile.getProperty("DB_USER").trim();
			DB_PSK = configFile.getProperty("DB_PSK").trim();
			GEYSER_ID = configFile.getProperty("GEYSER_ID").trim();
			START_TIMESTAMP = sdf.parse(configFile.getProperty("START_TIMESTAMP").trim());
			END_TIMESTAMP = sdf.parse(configFile.getProperty("END_TIMESTAMP").trim());
			OUTPUT_FILEPATH = configFile.getProperty("OUTPUT_FILEPATH").trim(); //Purely for results (NOT persistence)

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
    		results_writer = new PrintWriter(OUTPUT_FILEPATH, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	
    	
    	results_writer.println("Hello, world!");
    	results_writer.close();
    	logger.info("Geyser simulator finished");
    	
    }


    
}

