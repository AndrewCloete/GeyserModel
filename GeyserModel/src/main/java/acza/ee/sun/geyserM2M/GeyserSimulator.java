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
			writer.println("timestamp,usage,power,t_outlet(real),t_inside(sim),t_outlet(sim),t_inlet,t_ambient,energy");
		} catch (FileNotFoundException e) {
			logger.error("Output file not found: ", e);
			return;
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
			return;
		}
    	
    	
    	//Import usage points from file
    	//LinkedList<DataPoint> data_points = DataPoint.importUsageFromJSONFile(usage_filepath);
    	LinkedList<DataPoint> data_points = DataPoint.importUsageFromDatabase();
    	
    	//Create iterator.
    	ListIterator<DataPoint> data_iterator = data_points.listIterator();
    	
    	//Read first datapoint
    	DataPoint point = data_iterator.next();
    	
    	//Create and initialise new Geyser object
    	Geyser ewh = new Geyser(point.t_outlet);
    	
    	//Iterate through usage points and step simulation
    	while(data_iterator.hasNext()){
    	    
    		DataPoint next_point = data_iterator.next();	
    		ewh.setInletTemperature(point.t_inlet);
    		ewh.setAmbientTemperature(point.t_ambient);
    		ewh.stepUsage(point.usage_litres);
    		ewh.stepTime(next_point.timestamp - point.timestamp, point.power*1000);
    		writer.println(timestampToString(point.timestamp*1000) + "," + point.usage_litres + "," + point.power*1000 + "," + point.t_outlet + "," + ewh.toCSV());
    		
    		point = next_point;
    	}
    	writer.close();
    	logger.info("Geyser simulator finished");
    }

    private static String timestampToString(long stamp){
    	DateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm");
    	return df.format(new Date(stamp));
    }
    
}



