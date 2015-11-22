package acza.ee.sun.geyserM2M;

import java.util.LinkedList;
import java.util.ListIterator;

public class DataSummary {

	private DataSegment data_segment;
	public LinkedList<UsageEvent> hot_event_set;
	private double event_start_threshold;
	private double event_stop_threshold;
	
	private double total_volume;
	

	public DataSummary(DataSegment data_segment){
		
		this.data_segment = data_segment;
		this.event_start_threshold = 0.2;
		this.event_stop_threshold = 0.2;
		this.calculateHotEvents(this.event_start_threshold, this.event_stop_threshold);
	}
	
	public DataSummary(DataSegment data_segment, double event_start_threshold, double event_stop_threshold){
		
		this.data_segment = data_segment;
		this.event_start_threshold = event_start_threshold;
		this.event_stop_threshold = event_stop_threshold;
		this.calculateHotEvents(this.event_start_threshold, this.event_stop_threshold);
	}
	
	
	public void recalculateHotEvents(double start_threshold, double stop_threshold){
		this.event_start_threshold = start_threshold;
		this.event_stop_threshold = stop_threshold;
		this.calculateHotEvents(start_threshold, stop_threshold);
	}
	
	
	private void calculateHotEvents(double start_threshold, double stop_threshold){
		
		//Add stop TIME-threshold 
		
		hot_event_set = new LinkedList<UsageEvent>();
		
		long start_time = 0;
		double volume_counter = 0;
		double temp_out_sum = 0;
		double temp_in_sum = 0;
		int count = 0;
		
		
		int detect_state = 1;
		ListIterator<DataStamp> iterator = data_segment.stamp_set.listIterator();
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
	
	
	public LinkedList<UsageEvent> getLargeEvents(double boundary){
		
		LinkedList<UsageEvent> upper_events = new LinkedList<UsageEvent>();
		
    	ListIterator<UsageEvent> event_iterator = hot_event_set.listIterator();
    	while(event_iterator.hasNext()){
    		UsageEvent e = event_iterator.next();
    		if(e.volume >= boundary)
    			upper_events.add(e);
    	}
		
    	return upper_events;
	}
	
	public LinkedList<UsageEvent> getSmallEvents(double boundary){
		
		LinkedList<UsageEvent> lower_events = new LinkedList<UsageEvent>();
		
    	ListIterator<UsageEvent> event_iterator = hot_event_set.listIterator();
    	while(event_iterator.hasNext()){
    		UsageEvent e = event_iterator.next();
    		if(e.volume < boundary)
    			lower_events.add(e);
    	}
		
    	return lower_events;
	}
	
	public double getEventStartThreshold(){
		return this.event_start_threshold;
	}
	
	public double getEventStopThreshold(){
		return this.event_stop_threshold;
	}
	
	public double getTotalVolume(){
		return this.total_volume;
    	
	}
	
	public void summarise(){
		double count;
		double volume_counter = 0;
		double electric_counter = 0;
		
		ListIterator<DataStamp> data_iterator = data_segment.stamp_set.listIterator();
    	while(data_iterator.hasNext()){
    		DataStamp s = data_iterator.next();
    		
    		volume_counter += s.hot_dif;
    		electric_counter += s.kwatt_dif; 
    	}
	}
	
}
