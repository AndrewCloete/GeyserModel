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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.ListIterator;


public class GeyserSimulator 
{	
	private static final Logger logger = LogManager.getLogger(GeyserSimulator.class);
	private static PrintWriter writer;
	
	
    public static void main( String[] args )
    {
    	// ---------------------- Sanity checking of command line arguments ---------------------------------------------
		if( args.length != 2)
		{
			System.out.println( "Usage: <Input file path> <Output file path>" ) ;
			return;
		}
    	String usage_filepath = args[0];
    	//---------------------------------------------------------------------------------------------------------------
    	logger.info("Geyser simulator started with usage file: " + usage_filepath);
        
    	try {
			writer = new PrintWriter(args[1], "UTF-8");
			writer.println("ts,usages,NODE,t_lower,t_upper,t_inside,v_lower,v_upper");
		} catch (FileNotFoundException e) {
			logger.error("Output file not found: ", e);
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return;
		}
    	
    	//Create and initialise new Geyser object
    	Geyser ewh = new Geyser();
    	Thermostat thermostat = new Thermostat(50, 4);
    	
    	//Import usage points from file
    	LinkedList<UsagePoint> usage_points = UsagePoint.importUsageFromJSONFile(usage_filepath);
    	
    	//Iterate through usage points and step simulation
    	ListIterator<UsagePoint> usage_iterator = usage_points.listIterator();
    	UsagePoint point = usage_iterator.next();
    	while(usage_iterator.hasNext()){
    	    
    		UsagePoint next_point = usage_iterator.next();
    		ewh.setElement(thermostat.elementState(ewh.getInsideTemperature()));
    		
    		ewh.stepUsage(point.usage_litres);
    		ewh.stepTime(next_point.timestamp - point.timestamp);
    		writer.println(point.timestamp + "," + point.usage_litres + "," +ewh.toCSV());
    		
    		point = next_point;
    	}
    	writer.close();
    	logger.info("Geyser simulator finished");
    }

}



