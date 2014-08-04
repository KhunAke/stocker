package com.javath.trigger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;

import com.javath.util.Assign;
import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class MulticastEvent extends Instance implements Runnable {
	
	public static void send(String method, EventListener[] receivers, EventObject event) {
		MulticastEvent multicast = 
				(MulticastEvent) Assign.borrowObject(MulticastEvent.class);
		multicast.initial(method, receivers, event);
		
		TaskManager.create(String.format("%s", event), multicast);
	}
	
	private String method;
	private EventListener[] receivers;
	private EventObject event;
	
	public MulticastEvent() {}

	private void initial(String method, EventListener[] receivers, EventObject event) {
		this.method = method;
		this.receivers = receivers;
		this.event = event;
	}
	public void run() {
		for (int index = 0; index < receivers.length; index++) {
			try {
				EventListener receiver = receivers[index];
				Method method = receiver.getClass().getMethod(this.method, event.getClass());
				method.invoke(receiver, event);
			} catch (NoSuchMethodException e) {
				throw new ObjectException(e);
			} catch (SecurityException e) {
				throw new ObjectException(e);
			} catch (IllegalAccessException e) {
				throw new ObjectException(e);
			} catch (IllegalArgumentException e) {
				throw new ObjectException(e);
			} catch (InvocationTargetException e) {
				throw new ObjectException(e);
			} catch (ObjectException e) {}
		}
		Assign.returnObject(this);
	}
	
}
