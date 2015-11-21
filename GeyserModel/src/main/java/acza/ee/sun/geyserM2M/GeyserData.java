package acza.ee.sun.geyserM2M;

import java.util.LinkedList;
import java.util.ListIterator;

public class GeyserData {
	
	public LinkedList<DataStamp> stamp_set;
	public LinkedList<UsageEvent> hot_event_set;
	
	
	public GeyserData(LinkedList<DataStamp> stamp_set){
		this.stamp_set = stamp_set;
		
	}
	
	
	public void calculateHotEvents(double start_threshold, double stop_threshold){
		
		//Add stop TIME threshold 
		
		hot_event_set = new LinkedList<UsageEvent>();
		
		long start_time = 0;
		double volume_counter = 0;
		double temp_out_sum = 0;
		double temp_in_sum = 0;
		int count = 0;
		
		
		int detect_state = 1;
		ListIterator<DataStamp> iterator = stamp_set.listIterator();
		while(iterator.hasNext()){
			
			DataStamp p = iterator.next();
			
			switch(detect_state){
			case 1:	//Before: Wait for event to start
				if(p.hot_dif >= start_threshold){	//Start detected!
					start_time = p.server_stamp;
					volume_counter = p.hot_dif;
					temp_out_sum = p.t_outlet;
					temp_in_sum = p.t_inlet;
					count = 1;
					detect_state = 2;
				}
				break;
				
			case 2:	//During: Wait for event to end.
				
				if(p.hot_dif <= stop_threshold){	//Stop detected
					detect_state = 3;
				}
				else{
					volume_counter += p.hot_dif;
					temp_out_sum += p.t_outlet;
					temp_in_sum += p.t_inlet;
					count++;
				}
				
				break;
			
			case 3:	//After: Confirm and store
				
				if(p.hot_dif <= stop_threshold){	//Stop confirmed (1 minute confirm period)
					p = iterator.previous();
					//Sanity check
					long duration = p.server_stamp - start_time;
					//store result in list
					hot_event_set.add(new UsageEvent(	start_time, 
														volume_counter, 
														duration/(60*1000), 
														temp_out_sum/count, 
														temp_in_sum/count));
					detect_state = 1;
				}
				else{
					volume_counter += p.hot_dif;
					temp_out_sum += p.t_outlet;
					temp_in_sum += p.t_inlet;
					count++;
					detect_state = 2;
				}
				
				break;
			}
		}
	}
	
	public void diff(){
		
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
	
	
	
	//Create method for identifying gaps/inconsistencies in data
	

}
