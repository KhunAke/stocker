package com.javath.util;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonInitException;

import com.javath.logger.LOG;
import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorListener;

public class Service extends Instance implements Daemon, OscillatorListener {
	
	private final static Assign assign;
	private final static String[] ignore_monitor;
	
	static {
		String default_Properties = Assign.etc + Assign.File_Separator +
				"util" + Assign.File_Separator +
				"Service.properties";
		assign = Assign.getInstance(Service.class.getCanonicalName(), default_Properties);
		int ignore_length = (int) assign.getLongProperty("ignore_length", 0);
		ignore_monitor = new String[ignore_length];
		for (int index = 0; index < ignore_monitor.length; index++) {
			ignore_monitor[index] = assign.getProperty(
					String.format("ignore_monitor[%d]", index));
		}
		//ignore_monitor = new String[] {
		//	"^com.javath.trigger.Oscillator\\[period=[\\p{Digit}]+\\]$",
		//	"^com.javath.trigger.OscillatorEvent\\[timestamp=[\\p{Digit}]+\\]$"
		//};
	}
	
	private Thread shutdown;
	private Thread[] threads = null;
	//private boolean force = false;
    //private boolean stopped = false;
    //private boolean lastOneWasATick = false;
    
	@Override
	public void init(DaemonContext context) 
			throws DaemonInitException, Exception {
		INFO("Daemon initializing.");
		monitor(false);
		Oscillator.loader();
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
            shutdown.join(60000);
            //Thread.sleep(59900); // waitting time 1 min. 
            //force = true;
            WARNING("Thread force terminate.");
        }catch(InterruptedException e){
            INFO("Thread success terminate.");
        }
	}
	@Override
	public void destroy() {
		//if (force)
			 monitor(true);
		INFO("Daemon terminate.");
		//thread = null;
	}
	
	private boolean monitor(boolean show) {
		Map<Thread, StackTraceElement[]> map_thread = Thread.getAllStackTraces();
		Thread[] threads = map_thread.keySet().toArray(new Thread[] {});
		boolean result = false;
		if (this.threads == null) {
			this.threads = threads;
		} else {
			for (int index = 0; index < threads.length; index++) {
				if (!checkIgnoreMonitor(threads[index])) {
					result = true;
					if (show)
						WARNING("Thread: \"%s\" %s", 
								threads[index].getName(), threads[index].getState());
				}
			}
		}
		return result;
	}
	private boolean checkIgnoreMonitor(Thread thread) {
		boolean result = false;
		for (int index = 0; index < ignore_monitor.length; index++) 
			if (Pattern.matches(ignore_monitor[index], thread.getName()))
				return true;
		for (int index = 0; index < threads.length; index++)
			if (thread.getName().equals(threads[index].getName())) {
				result = true;
				break;
			}
		return result;
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
		if (!monitor(false))
			shutdown.interrupt();
	}

}
