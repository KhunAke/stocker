package com.javath.util;

import java.util.Map;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;

import com.javath.logger.LOG;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorListener;

import util.TestOscillator;

public class Service extends Instance implements Daemon, OscillatorListener {
	
	private final static Assign assign;
	private final static long waiting;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"util" + Assign.File_Separator +
				"Service.properties";
		assign = Assign.getInstance(Service.class.getCanonicalName(), default_Properties);
		waiting = assign.getLongProperty("waiting", 300000);
	}
	
	private Thread shutdown;
	private Thread[] threads; 
    //private boolean stopped = false;
    //private boolean lastOneWasATick = false;
    
	@Override
	public void init(DaemonContext context) 
			throws DaemonInitException, Exception {
		INFO("Daemon initializing.");
		Oscillator.loader();
		monitor(false);
	}
	@Override
	public void start() 
			throws Exception {
		INFO("Daemon starting.");
		Oscillator.startAll();
	}
	@Override
	public void stop() 
			throws Exception {
		INFO("Daemon stopping.");
		//stopped = true;
		Oscillator.shutdown();
		shutdown = Thread.currentThread();
		try{
			Oscillator oscillator = Oscillator.getInstance(1000);
            oscillator.addListener(this);
            oscillator.start();
            Thread.sleep(waiting);
            LOG.WARNING("Thread force terminate.");
        }catch(InterruptedException e){
            LOG.INFO("Thread success terminate.");
        }
	}
	@Override
	public void destroy() {
		INFO("Daemon terminate.");
		//thread = null;
	}
	
	private void monitor(boolean member) {
		Map<Thread, StackTraceElement[]> map_thread = Thread.getAllStackTraces();
		Thread[] threads = map_thread.keySet().toArray(new Thread[] {});
		if (member)
			this.threads = threads;
		for (int index = 0; index < threads.length; index++) {
			System.out.printf("\"%s\" %s%n", threads[index].getName(), threads[index].getState());
		}
	}
	
	public static void main(String[] args) {
		try {
			DaemonContext context = new DaemonContext() {
					private String[] args;
					@Override
					public String[] getArguments() {
						return args;
					}
					@Override
					public DaemonController getController() {
						return null;
					}
					public DaemonContext setArguments(String[] args) {
						this.args = args;
						return this;
					}
				}.setArguments(args);
			Service service = new Service();
			service.init(context);
			service.start();
		} catch (DaemonInitException e) {
			LOG.SEVERE(e);
		} catch (Exception e) {
			LOG.SEVERE(e);
		}
	}
	
	@Override
	public void action(OscillatorEvent event) {
		shutdown.interrupt();
	}

}
