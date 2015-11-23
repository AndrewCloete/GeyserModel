package acza.ee.sun.geyserM2M;

import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DataSummary {

	private DataSegment data_segment;
	private LinkedList<UsageEvent> event_set;
	private double event_start_threshold;
	private double event_stop_threshold;
	private double event_vol_boundary; //In litres
	private double event_dur_boundary; //In minutes
	
	public DescriptiveStatistics flow_stats = new DescriptiveStatistics();
	public DescriptiveStatistics kwatt_stats = new DescriptiveStatistics();
	public DescriptiveStatistics t_outlet_stats = new DescriptiveStatistics();
	public DescriptiveStatistics t_far_stats = new DescriptiveStatistics();
	public DescriptiveStatistics t_inlet_stats = new DescriptiveStatistics();
	public DescriptiveStatistics t_ambient_stats = new DescriptiveStatistics();
	
	public DescriptiveStatistics events_vol_stats = new DescriptiveStatistics();
	public DescriptiveStatistics events_dur_stats = new DescriptiveStatistics();
	public DescriptiveStatistics l_events_volBYvol_stats = new DescriptiveStatistics();
	public DescriptiveStatistics l_events_durBYvol_stats = new DescriptiveStatistics();
	public DescriptiveStatistics s_events_volBYvol_stats = new DescriptiveStatistics();
	public DescriptiveStatistics s_events_durBYvol_stats = new DescriptiveStatistics();
	

	public DataSummary(DataSegment data_segment){
		
		this.data_segment = data_segment; 
		this.event_start_threshold = 0.2;
		this.event_stop_threshold = 0.2;
		this.event_vol_boundary = 10;
		this.event_dur_boundary = 5;
		this.calculateEvents(this.event_start_threshold, this.event_stop_threshold);
		this.summarise();
	}
	
	public DataSummary(DataSegment data_segment, double event_start_threshold, double event_stop_threshold, double eventsize_vol_boundary, double eventsize_dur_boundary){
		
		this.data_segment = data_segment;
		this.event_start_threshold = event_start_threshold;
		this.event_stop_threshold = event_stop_threshold;
		this.event_vol_boundary = eventsize_vol_boundary;
		this.event_dur_boundary = eventsize_dur_boundary;
		this.calculateEvents(this.event_start_threshold, this.event_stop_threshold);
		this.summarise();
	}
	
	
	public void recalculateEvents(double start_threshold, double stop_threshold){
		this.event_start_threshold = start_threshold;
		this.event_stop_threshold = stop_threshold;
		this.calculateEvents(start_threshold, stop_threshold);
	}
	
	
	private void calculateEvents(double start_threshold, double stop_threshold){
		
		//Add stop TIME-threshold 
		
		event_set = new LinkedList<UsageEvent>();
		
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
					event_set.add(new UsageEvent(	start_time, 
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
	
	public LinkedList<UsageEvent> getAllEvents(){
		return this.event_set;
	}
	

	public LinkedList<UsageEvent> splitEventsByVolume(double boundary, boolean upperNotLower){
		
		LinkedList<UsageEvent> lower_events = new LinkedList<UsageEvent>();
		
    	ListIterator<UsageEvent> event_iterator = event_set.listIterator();
    	while(event_iterator.hasNext()){
    		UsageEvent e = event_iterator.next();
    		
    		if(upperNotLower){
	    		if(e.volume >= boundary)
	    			lower_events.add(e);
    		}
    		else{
    			if(e.volume < boundary)
	    			lower_events.add(e);
    		}
    	}
		
    	return lower_events;
	}
	
	public LinkedList<UsageEvent> splitEventsByDuration(double boundary, boolean upperNotLower){
		
		LinkedList<UsageEvent> lower_events = new LinkedList<UsageEvent>();
		
    	ListIterator<UsageEvent> event_iterator = event_set.listIterator();
    	while(event_iterator.hasNext()){
    		UsageEvent e = event_iterator.next();
    		
    		if(upperNotLower){
	    		if(e.duration >= boundary)
	    			lower_events.add(e);
    		}
    		else{
    			if(e.duration < boundary)
	    			lower_events.add(e);
    		}
    	}
		
    	return lower_events;
	}
	
	public UsageEvent getMaxEventByVolume(){
		UsageEvent maxEvent;
		
    	ListIterator<UsageEvent> event_iterator = event_set.listIterator();
    	
    	maxEvent = event_iterator.next();
    	while(event_iterator.hasNext()){
    		UsageEvent e = event_iterator.next();
    		
    		if(e.volume > maxEvent.volume)
    			maxEvent = e;
    	}
		
    	return maxEvent;
	}
	
	public UsageEvent getMaxEventByDuration(){
		UsageEvent maxEvent;
		
    	ListIterator<UsageEvent> event_iterator = event_set.listIterator();
    	
    	maxEvent = event_iterator.next();
    	while(event_iterator.hasNext()){
    		UsageEvent e = event_iterator.next();
    		
    		if(e.duration > maxEvent.duration)
    			maxEvent = e;
    	}
		
    	return maxEvent;
	}
	
	public LinkedList<UsageEvent> getLargeVolEvents(){
		return splitEventsByVolume(this.event_vol_boundary, true);
	}
	

	public LinkedList<UsageEvent> getSmallVolEvents(){
		return splitEventsByVolume(this.event_vol_boundary, false);
	}
	
	
	public LinkedList<UsageEvent> getLongDurEvents(){
		return splitEventsByDuration(this.event_dur_boundary, true);
	}
	
	public LinkedList<UsageEvent> getShortDurEvents(){
		return splitEventsByDuration(this.event_dur_boundary, false);
	}
	
	
	public double getEventStartThreshold(){
		return this.event_start_threshold;
	}
	
	public double getEventStopThreshold(){
		return this.event_stop_threshold;
	}
	
	
	private void summarise(){
		
		ListIterator<DataStamp> data_iterator = data_segment.stamp_set.listIterator();
    	while(data_iterator.hasNext()){
    		DataStamp s = data_iterator.next();
    		
    		flow_stats.addValue(s.hot_dif);
    		kwatt_stats.addValue(s.kwatt_dif);
    		t_far_stats.addValue(s.t_far);
    		t_outlet_stats.addValue(s.t_outlet);
    		t_inlet_stats.addValue(s.t_inlet);
    		t_ambient_stats.addValue(s.t_ambient);
    	}
    	
    	ListIterator<UsageEvent> all_event_iterator = this.event_set.listIterator();
    	while(all_event_iterator.hasNext()){
    		UsageEvent e = all_event_iterator.next();
    		
    		events_vol_stats.addValue(e.volume);
    		events_dur_stats.addValue(e.duration);
    	}
    	
    	ListIterator<UsageEvent> large_event_iterator = this.getLargeVolEvents().listIterator();
    	while(large_event_iterator.hasNext()){
    		UsageEvent e = large_event_iterator.next();
    		
    		l_events_volBYvol_stats.addValue(e.volume);
    		l_events_durBYvol_stats.addValue(e.duration);
    	}
    	
    	ListIterator<UsageEvent> small_event_iterator = this.getSmallVolEvents().listIterator();
    	while(small_event_iterator.hasNext()){
    		UsageEvent e = small_event_iterator.next();
    		
    		s_events_volBYvol_stats.addValue(e.volume);
    		s_events_durBYvol_stats.addValue(e.duration);
    	}
	}
	
	public static String CSV_HEADER = "total_volume,total_elec_energy," +
								"#events,#l_eventsBYvol,#s_eventsBYvol," + 
								"events_mean_vol,l_events_mean_vol,s_events_mean_vol," + 
								"events_mean_dur,l_events_mean_dur,s_events_mean_dur," +
								"maxv_event_vol,maxv_event_dur,maxd_event_vol,maxd_event_dur";
	
	public String toString(){
		
		UsageEvent maxv = this.getMaxEventByVolume();
		UsageEvent maxd = this.getMaxEventByDuration();
		
		return "" + 
				flow_stats.getSum() + "," + 
				kwatt_stats.getSum() + "," + 
				events_vol_stats.getN() + "," + l_events_volBYvol_stats.getN() + "," + s_events_volBYvol_stats.getN() + "," +
				events_vol_stats.getMean() + "," + l_events_volBYvol_stats.getMean() + "," + s_events_volBYvol_stats.getMean() + "," +
				events_dur_stats.getMean() + "," + l_events_durBYvol_stats.getMean() + "," + s_events_durBYvol_stats.getMean() + "," +
				maxv.volume + "," +  maxv.duration + "," + maxd.volume + "," +  maxd.duration;
	}
	
}
