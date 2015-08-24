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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
			writer.println("ts,usages,NODE,element_state,t_inside,t_lower,t_upper,v_lower,v_upper,t1");
		} catch (FileNotFoundException e) {
			logger.error("Output file not found: ", e);
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return;
		}
    	
    	
    	//Import usage points from file
    	LinkedList<UsagePoint> usage_points = UsagePoint.importUsageFromJSONFile(usage_filepath);
    	
    	//Create iterator.
    	ListIterator<UsagePoint> usage_iterator = usage_points.listIterator();
    	
    	//Read first datapoint
    	UsagePoint point = usage_iterator.next();
    	
    	//Create and initialise new Geyser object
    	Geyser ewh = new Geyser(point.t1);
    	
    	//Iterate through usage points and step simulation
    	while(usage_iterator.hasNext()){
    	    
    		UsagePoint next_point = usage_iterator.next();	
    		ewh.setElement(point.element_state);
    		ewh.stepUsage(point.usage_litres);
    		ewh.stepTime(next_point.timestamp - point.timestamp);
    		writer.println(timestampToString(point.timestamp*1000) + "," + point.usage_litres + "," +ewh.toCSV() + "," + point.t1);
    		System.out.println(point.timestamp);
    		
    		point = next_point;
    	}
    	writer.close();
    	logger.info("Geyser simulator finished");
    }

    private static String timestampToString(long stamp){
    	DateFormat df = new SimpleDateFormat("yy/MM/dd kk:mm");
    	return df.format(new Date(stamp));
    }
    
}



