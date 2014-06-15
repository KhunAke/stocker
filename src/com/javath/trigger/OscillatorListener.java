package com.javath.trigger;

import java.util.EventListener;

public interface OscillatorListener extends EventListener {
	
	public void action(OscillatorEvent event);

}
