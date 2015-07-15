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

import java.util.LinkedList;
import java.util.ListIterator;


public class GeyserSimulator 
{	
	private static final Logger logger = LogManager.getLogger(GeyserSimulator.class);
	
    public static void main( String[] args )
    {
    	logger.info("Geyser model started.");
        
    	LinkedList<UsagePoint> usage_points = new LinkedList<UsagePoint>();
    	
    	//Test points
    	usage_points.add(new UsagePoint(1436974356, 0));
    	usage_points.add(new UsagePoint(1436974357, 0));
    	usage_points.add(new UsagePoint(1436974358, 1));
    	usage_points.add(new UsagePoint(1436974359, 0));
    	usage_points.add(new UsagePoint(1436974360, 0));
    	
    	Geyser ewh = new Geyser();
    	ewh.stepUsage(32);
    	
    	ListIterator<UsagePoint> usage_iterator = usage_points.listIterator();
    	
    	UsagePoint point = usage_iterator.next();
    	while(usage_iterator.hasNext()){
    	    
    		UsagePoint next_point = usage_iterator.next();
    		
    		System.out.println("Point: " + point.timestamp + "," + point.usage_litres);
    		
    		ewh.stepUsage(point.usage_litres);
    		ewh.stepTime(next_point.timestamp - point.timestamp);
    		System.out.println(ewh.toJSON());
    		
    		point = next_point;
    	}
    }

}



