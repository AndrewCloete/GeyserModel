package acza.ee.sun.geyserM2M;

import java.util.LinkedList;
import java.util.ListIterator;

public class DataSegment {
	
	public LinkedList<DataStamp> stamp_set;
	
	private final String ewh_id;
	private final long start_time;
	private final long end_time;
	private final int stamp_count;
	//Other integrity metrics
	
	
	public DataSegment(LinkedList<DataStamp> stamp_set){
		this.stamp_set = stamp_set;
		this.ewh_id = this.stamp_set.getFirst().ewh_id;
		this.diff();
		this.start_time = this.stamp_set.getFirst().server_stamp;
		this.end_time = this.stamp_set.getLast().server_stamp;
		this.stamp_count = this.stamp_set.size();
		
		//Report data integrity metrics.
	}
	
	
	/* Methods for identifying gaps, inconsistencies and other integrity issues in data
	 * 	Sum hot_dif and hotflow_rpm 
	 * 	Server- and client stamp comparison.
	 */
	
	public String getEWHid(){
		return this.ewh_id;
	}
	
	public long getStartTime(){
		
		return this.start_time;
	}
	
	public long getEndTime(){
		
		return end_time;
	}
	
	public long getDeltaTime(){
		return this.end_time - start_time;
	}
	
	public int getStampCount(){
		
		return stamp_count;
	}
	
	public double calulatePacketLoss(){
		return stamp_count/(this.getDeltaTime()/1000); // #stamps/timedif_in_secs
	}
	
	
	private void diff(){
		
		ListIterator<DataStamp> iterator = this.stamp_set.listIterator();
		
		DataStamp current_point = iterator.next();
		
		
		while(iterator.hasNext()){
		
			DataStamp next_point = iterator.next();
			
			//if difference between timestamps is more than 10 minutes, give warning.
			
			
			double hot_dif = next_point.hot_litres_tot - current_point.hot_litres_tot;
			double cold_dif = next_point.cold_litres_tot - current_point.cold_litres_tot;
			double kwatt_dif = next_point.kwatt_tot - current_point.kwatt_tot;
		
			if(!(hot_dif <= 0))
				next_point.hot_dif = hot_dif;
			
			if(!(cold_dif <= 0))
				next_point.cold_dif = cold_dif;
				
			if(!(kwatt_dif <= 0))
				next_point.kwatt_dif = kwatt_dif;
			
			current_point = next_point;
			
		}
	}
	

}
