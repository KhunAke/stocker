package com.javath.util;

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
		waiting = assign.getLongProperty("waiting");
	}
	
	private Thread thread; 
    //private boolean stopped = false;
    //private boolean lastOneWasATick = false;
    
	@Override
	public void init(DaemonContext context) 
			throws DaemonInitException, Exception {
		LOG.INFO("[@%d] Daemon initializing.", this.InstanceId);
		Oscillator.loader();
	}
	@Override
	public void start() 
			throws Exception {
		LOG.INFO("[@%d] Daemon starting.", this.InstanceId);
		Oscillator.startAll();
	}
	@Override
	public void stop() 
			throws Exception {
		LOG.INFO("[@%d] Daemon stopping.", this.InstanceId);
		//stopped = true;
		Oscillator.shutdown();
		thread = Thread.currentThread();
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
		LOG.INFO("[@%d] terminate.", this.InstanceId);
		//thread = null;
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
		thread.interrupt();
	}

}
