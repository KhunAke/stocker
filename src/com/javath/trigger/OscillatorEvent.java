package com.javath.trigger;

import java.util.EventObject;

public class OscillatorEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	
	private final long timestamp;

	public OscillatorEvent(Object source) {
		this(source, 0);
	}
	
	public OscillatorEvent(Object source, long timestamp) {
		super(source);
		this.timestamp  = timestamp ;
	}
	
	public long getPeriod() {
		return ((Oscillator) source).getPeriod();
	}
	
	public long getTimestamp() {
		return timestamp ;
	}
	
	public String toString() {
		return String.format("%s[timestamp=%d]", 
				this.getClass().getCanonicalName(), timestamp);
	}

}
