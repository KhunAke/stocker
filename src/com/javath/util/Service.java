package com.javath.util;

import java.io.File;
import java.util.Set;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import com.javath.trigger.Oscillator;
import com.javath.trigger.OscillatorEvent;
import com.javath.trigger.OscillatorListener;

import util.TestOscillator;

public class Service extends Instance implements Daemon, OscillatorListener {
	
	private final static Assign assign;
	private final static long waiting;
	
	static {
		String default_Properties = Assign.etc + Assign.FILE_SEPARATOR +
				"util" + Assign.FILE_SEPARATOR +
				"Service.properties";
		assign = Assign.getInstance(Service.class.getCanonicalName(), default_Properties);
		waiting = assign.getPropertyLong("waiting");
	}
	
	private Thread thread; 
    //private boolean stopped = false;
    //private boolean lastOneWasATick = false;
    
	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		INFO("Daemon initializing.");
		/*
         * Construct objects and initialize variables here.
         * You can access the command line arguments that would normally be passed to your main() 
         * method as follows:
         */
		//String[] args = context.getArguments();
		/*
		thread = new Thread("tick_tock") {
            private long lastTick = 0;
            
            @Override
            public synchronized void start() {
            	stopped = false;
                super.start();
            }

            @Override
            public void run() {             
                while(!stopped){
                    long now = System.currentTimeMillis();
                    if(now - lastTick >= 1000){
                        System.out.println(!lastOneWasATick ? "tick" : "tock");
                        lastOneWasATick = !lastOneWasATick;
                        lastTick = now; 
                    }
                }
            }
        };*/
		new TestOscillator();
	}
	@Override
	public void start() throws Exception {
		INFO("Daemon starting.");
		//thread.start();
	}
	@Override
	public void stop() throws Exception {
		INFO("Daemon stopping.");
		//stopped = true;
		thread = Thread.currentThread();
		try{
			Oscillator oscillator = Oscillator.getInstance(240000);
            oscillator.addListener(this);
            oscillator.start();
            Thread.sleep(waiting);
            WARNING("Thread force terminate.");
        }catch(InterruptedException e){
            INFO("Thread success terminate.");
        }
	}
	@Override
	public void destroy() {
		INFO("Daemon done.");
		//thread = null;
	}
	
	public static void main(String[] args) {
		//Date date = new Date();
		// TODO Auto-generated method stub
		System.out.println("Hello");
	}
	
	@Override
	public void action(OscillatorEvent event) {
		thread.interrupt();
	}

}
