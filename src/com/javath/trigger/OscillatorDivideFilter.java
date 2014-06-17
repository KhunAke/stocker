package com.javath.trigger;

import com.javath.util.TaskManager;

public class OscillatorDivideFilter implements OscillatorListener {
	
	private final Oscillator source;
	private final OscillatorListener destination; 
	
	private long period;
	private long schedule;
	
	public OscillatorDivideFilter(Oscillator source, OscillatorListener destination, long period) {
		this.source = source;
		this.destination = destination;
		this.period = period;
		this.source.addListener(this);
	}
	public OscillatorDivideFilter(Oscillator source, OscillatorListener destination, long period, long  timestamp) {
		this(source, destination, period);
		setSchedule(timestamp);
	}
	
	public Oscillator getSource() {
		return source;
	}
	
	public void setSchedule(long timestamp) {
		this.schedule = timestamp / source.getPeriod();
	}
	public long getSchedule() {
		return schedule * source.getPeriod();
	}

	@Override
	public void action(OscillatorEvent event) {
		long timestamp = event.getTimestamp() / event.getPeriod();
		//System.err.printf("%d = %d%n", schedule ,timestamp);
		if ((schedule <= timestamp) && ((timestamp - schedule) % period == 0))
			TaskManager.create(destination, "action", event);
	}
	
}
