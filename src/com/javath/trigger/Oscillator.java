package com.javath.trigger;

import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.javath.util.ObjectException;

public class Oscillator extends TimerTask implements OscillatorSource, Runnable {
	
	private static final Map<Long,Oscillator> instances;
	
	static {
		instances = new HashMap<Long,Oscillator>();
	}
	
	public static Oscillator getInstance(long period) {
		Oscillator instance = instances.get(period);
		if (instance == null) {
			instance = new Oscillator();
			instance.setPeriod(period);
			instances.put(period, instance);
		}
		return instance;
	}
	
	public static void startAll() {
		Set<Long> keys = instances.keySet();
		for (Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
			Long key = iterator.next();
			instances.get(key).start();
		}
	}
	public static void stopAll() {
		Set<Long> keys = instances.keySet();
		for (Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
			Long key = iterator.next();
			instances.get(key).stop();
		}
	}
	
	private long period;
	private final Set<OscillatorListener> listeners;
	private Timer timer;
	private boolean active = false;
	
	private Oscillator() {
		listeners = new HashSet<OscillatorListener>();
	}
	
	private void setPeriod(long period) {
		this.period = period;
		this.timer = new Timer(String.format("%s[period=%d]", 
				this.getClass().getCanonicalName(), period));
		long delay = period - (new Date().getTime() % period); 
		this.timer.scheduleAtFixedRate(this, delay, period);
	}
	public long getPeriod() {
		return period;
	}
	
	@Override
	public boolean addListener(OscillatorListener listener) {
		return listeners.add(listener);
	}
	@Override
	public boolean removeListener(OscillatorListener listener) {
		return listeners.remove(listener);
	}
	
	public void start() {
		active = true;
	}
	public void stop() {
		active = false;
	}
	
	@Override
	public void run() {
		if (active) {
			try {
				EventListener[] listeners = new EventListener[] {};
				listeners = this.listeners.toArray(listeners);
				if (listeners.length > 0 ) {
					OscillatorEvent event = new OscillatorEvent( this, new Date().getTime());
					MulticastEvent.send("action", listeners, event);
				}
			} catch (NoSuchElementException e) {
				throw new ObjectException(e);
			} catch (IllegalStateException e) {
				throw new ObjectException(e);
			} catch (Exception e) {
				throw new ObjectException(e);
			}
		}
	}
	
}
