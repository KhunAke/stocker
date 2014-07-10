package com.javath.trigger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.regex.Pattern;

import com.javath.logger.LOG;
import com.javath.util.Assign;
import com.javath.util.ObjectException;
import com.javath.util.Service;

public class Oscillator extends TimerTask implements Runnable {
	
	private final static Assign assign;
	private static final Map<Long,Oscillator> instances;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"trigger" + Assign.File_Separator +
				"Oscillator.properties";
		assign = Assign.getInstance(Oscillator.class, default_Properties);
		instances = new HashMap<Long,Oscillator>();
	}
	
	public static Oscillator getInstance(long period) {
		synchronized (instances) {
			Oscillator instance = instances.get(period);
			if (instance == null) {
				instance = new Oscillator();
				instance.setPeriod(period);
				instances.put(period, instance);
			}
			return instance;
		}
	}
	
	public static void loader() {
		String loader_path = Assign.etc + Assign.File_Separator + "OscillatorLoader";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
					new FileReader(assign.getProperty("OscillatorLoader", loader_path)));
			while (reader.ready()) {
				String line = reader.readLine();
				String classname = null;
				String method_name = null;
				String[] arguments = null;
				String pattern_not_arguments = "^\\w+(.\\w+)*\\(\\s*\\)$";
				String pattern_arguments = "^\\w+(.\\w+)*\\(\\s*\\w+\\s*(\\s*,\\s*\\w+)*\\)$";
				if (Pattern.matches(pattern_not_arguments, line)) {
					classname = line.substring(0,line.indexOf('('));
					method_name = "getInstance";
				} else if (Pattern.matches(pattern_arguments, line)) {
					classname = line.substring(0,line.indexOf('('));
					method_name = "getInstance";
					arguments = line.substring(line.indexOf('(') + 1, line.indexOf(')'))
							.split(",\\s");
				}
				Class<?> clazz = null;
				OscillatorLoader object = null;
				try {
					clazz = Class.forName(classname);
					if (OscillatorLoader.class.isAssignableFrom(clazz)) {
						if (arguments == null)
							object = (OscillatorLoader) Assign.forConstructor(classname);
						else
							object = (OscillatorLoader) Assign.forConstructor(classname, arguments);
					}
				} catch (ClassNotFoundException e) {
					method_name = classname.substring(classname.lastIndexOf('.'));
					classname = classname.substring(0, classname.lastIndexOf('.'));
					try {
						clazz = Class.forName(classname);
						if (OscillatorLoader.class.isAssignableFrom(clazz)) {
							if (arguments == null)
								object = (OscillatorLoader) Assign.forMethod(classname, method_name);
							else
								object = (OscillatorLoader) Assign.forMethod(classname, method_name, arguments);
						}
					} catch (ClassNotFoundException ex) {
						LOG.CONFIG(new ObjectException(e, String.format(
								"Classloader \"%1$s\" and \"%1$s.%2$s\" not found", classname, method_name)));
						continue;
					}
				}
				if (object != null) {
					object.initOscillator();
					LOG.INFO("\"%s\" loaded.", line.replaceAll("\\s", ""));
				} else 
					LOG.INFO("\"%s\" not load.", line.replaceAll("\\s", ""));
				/**
				try {
					Class<?> clazz = Class.forName(classname);
					if (OscillatorLoader.class.isAssignableFrom(clazz)) {
						LOG.INFO("\"%s\" loaded.", classname);
						try {
							//Object object;
							try {
								if (argument) {
									Object[] arguments = 
											line.substring(line.indexOf('(') + 1, line.indexOf(')'))
											.split(",\\s");
									Class<?>[] types = new Class<?>[arguments.length];
									Method method = clazz.getMethod("getInstance", types);
									object = method.invoke(null, arguments);
								} else
									object = clazz.newInstance();
							} catch (IllegalAccessException e) {
								Method method = clazz.getMethod("getInstance");
								object = method.invoke(null);
							} catch (InstantiationException e) {
								Method method = clazz.getMethod("getInstance");
								object = method.invoke(null);
							}
							Method method = clazz.getMethod("initOscillator");
							method.invoke(object);
						} catch (NoSuchMethodException e) {
							LOG.CONFIG(e);
						} catch (SecurityException e) {
							LOG.CONFIG(e);
						} catch (IllegalAccessException e) {
							LOG.CONFIG(e);
						} catch (IllegalArgumentException e) {
							LOG.CONFIG(e);
						} catch (InvocationTargetException e) {
							LOG.CONFIG(e);
						}
					} else
						LOG.INFO("\"%s\" not load.", classname);
				} catch (ClassNotFoundException e) {
					LOG.CONFIG(e);
				} 
				/**/
			}
			reader.close();
		} catch (FileNotFoundException e) {
			LOG.CONFIG(e);
		} catch (IOException e) {
			LOG.CONFIG(e);
		} 
	}
	public static void startAll() {
		synchronized (instances) {
			Set<Long> keys = instances.keySet();
			for (Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
				Long key = iterator.next();
				instances.get(key).start();
			}
		}
	}
	public static void stopAll() {
		synchronized (instances) {
			Set<Long> keys = instances.keySet();
			for (Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
				Long key = iterator.next();
				instances.get(key).stop();
			}
		}
	}
	public static void shutdown() {
		synchronized (instances) {
			Set<Long> keys = instances.keySet();
			for (Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
				Long key = iterator.next();
				instances.get(key).removeListener();
			}
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
	
	public boolean addListener(OscillatorListener listener) {
		return listeners.add(listener);
	}
	private void removeListener() {
		listeners.clear();
	}
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
				} else { // 
					synchronized (instances) {
						instances.remove(period);
						timer.cancel();
					}
				}
			} catch (NoSuchElementException e) {
				LOG.SEVERE(e);
			} catch (IllegalStateException e) {
				LOG.SEVERE(e);
			} catch (Exception e) {
				LOG.SEVERE(e);
			}
		}
	}
	
	public void destroy() {
		synchronized (instances) {
			instances.remove(period);
			timer.cancel();
		}
	}
	
}
