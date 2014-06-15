package com.javath.trigger;

public interface OscillatorSource {
	public boolean addListener(OscillatorListener listener);
	public boolean removeListener(OscillatorListener listener);
}
