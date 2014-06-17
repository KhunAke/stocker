package com.javath.trigger;

import com.javath.logger.LOG;
import com.javath.util.DateTime;
import com.javath.util.TaskManager;

public class OscillatorScheduleFilter implements OscillatorListener {
	
	private final Oscillator source;
	private final OscillatorListener destination; 

	private long schedule;
	
	public OscillatorScheduleFilter(Oscillator source, OscillatorListener destination) {
		this.source = source;
		this.destination = destination;
	}
	public OscillatorScheduleFilter(Oscillator source, OscillatorListener destination, long timestamp) {
		this(source, destination);
		setSchedule(timestamp);
	}
	
	public Oscillator getSource() {
		return source;
	}
	
	public void setSchedule(long timestamp) {
		this.schedule = timestamp / source.getPeriod();
		source.addListener(this);
	}
	public long getSchedule() {
		return schedule * source.getPeriod();
	}

	@Override
	public void action(OscillatorEvent event) {
		long timestamp = event.getTimestamp() / event.getPeriod();
		//System.err.printf("%d = %d%n", schedule ,timestamp);
		if (schedule == timestamp) {
			TaskManager.create(destination, "action", event);
			source.removeListener(this);
		} else if (schedule <= timestamp) {
			TaskManager.create(destination, "action", event);
			source.removeListener(this);
			LOG.WARNING("Oscillator[Period=%d] Schedule mismatch at \"%s\"", 
					source.getPeriod(), DateTime.timestamp(getSchedule()));
		}
	}
	
}
