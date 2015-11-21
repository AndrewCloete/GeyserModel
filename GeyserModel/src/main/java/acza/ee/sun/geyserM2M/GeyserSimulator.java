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
	
	private static String DB_URL;
	private static String DB_USER;
	private static String DB_PSK;
	private static String GEYSER_ID;
	private static Date START_TIMESTAMP;
	private static Date END_TIMESTAMP;
	private static String OUTPUT_FILEPATH;
	
	
    public static void main( String[] args )
    {
    	// ---------------------- Sanity checking of command line arguments ---------------------------------------------
    		//None for now
    	//---------------------------------------------------------------------------------------------------------------
    	
    	// ---------------------- Reading and sanity checking configuration parameters -------------------------------------------
    	Properties configFile = new Properties();
    	try {
    		configFile.load(GeyserSimulator.class.getClassLoader().getResourceAsStream("config.properties"));
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
        
    	
    	GeyserSQLDatabase gdb = new GeyserSQLDatabase(DB_URL, DB_USER, DB_PSK);
    	LinkedList<DataStamp> data_points = gdb.select(GEYSER_ID, START_TIMESTAMP, END_TIMESTAMP);
    	GeyserData gd = new GeyserData(data_points);
    	gd.diff();
    	gd.calculateHotEvents(0.3, 0.2);
    	
    	
    	
    	
    	try {
    		results_writer = new PrintWriter(OUTPUT_FILEPATH, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    	
    	//Write events to file
    	ListIterator<UsageEvent> event_iterator = gd.hot_event_set.listIterator();
    	results_writer.println(UsageEvent.CSV_HEADER);
    	while(event_iterator.hasNext()){
    		results_writer.println(event_iterator.next());
    	}
    	
    	//Write separation line
    	results_writer.println();
    	results_writer.println();
    	
    	
    	//Write raw data to file
    	ListIterator<DataStamp> stamp_iterator = gd.stamp_set.listIterator();
    	results_writer.println(DataStamp.CSV_HEADER);
    	while(stamp_iterator.hasNext()){
    		results_writer.println(stamp_iterator.next());
    	}
    	

    
    	
    	results_writer.close();
    	logger.info("Geyser simulator finished");
    	
    }


    
}


