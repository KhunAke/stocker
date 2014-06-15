package com.javath.util;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

public class Service extends Instance implements Daemon {
	
	private Thread thread; 
    private boolean stopped = false;
    private boolean lastOneWasATick = false;

	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		INFO("Daemon initializing.");
		/*
         * Construct objects and initialize variables here.
         * You can access the command line arguments that would normally be passed to your main() 
         * method as follows:
         */
		//String[] args = context.getArguments();
		thread = new Thread() {
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
        };
	}
	@Override
	public void start() throws Exception {
		INFO("Daemon starting.");
		thread.start();
	}
	@Override
	public void stop() throws Exception {
		INFO("Daemon stopping.");
		stopped = true;
        try{
            thread.join(1000);
        }catch(InterruptedException e){
            System.err.println(e.getMessage());
            throw e;
        }
	}
	@Override
	public void destroy() {
		INFO("Daemon done.");
		thread = null;
	}
	
	public static void main(String[] args) {
		//Date date = new Date();
		// TODO Auto-generated method stub
		System.out.println("Hello");
	}

}
