package com.javath.trigger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.EventObject;
import java.util.NoSuchElementException;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import com.javath.util.Instance;
import com.javath.util.ObjectException;
import com.javath.util.TaskManager;

public class MulticastEvent extends Instance implements Runnable {

	private static ObjectPool<MulticastEvent> pool;
	
	static {
		pool = new GenericObjectPool<MulticastEvent>(
				new PoolableObjectFactory<MulticastEvent>() {
					@Override
					public MulticastEvent makeObject() 
							throws Exception {
						return new MulticastEvent();
					}
					@Override
					public void activateObject(MulticastEvent event) 
							throws Exception {}
					@Override
					public void passivateObject(MulticastEvent event) 
							throws Exception {}
					@Override
					public boolean validateObject(MulticastEvent event) {
						return false;
					}
					@Override
					public void destroyObject(MulticastEvent event) 
							throws Exception {}
				});
	}

	private static MulticastEvent borrowObject() {
		try {
			return pool.borrowObject();
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	
	private static void returnObject(MulticastEvent multicast) {
		try {
			pool.returnObject(multicast);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	
	public static void send(String method,EventListener[] receivers,EventObject event) {
		MulticastEvent multicast = borrowObject();
		multicast.initial(method, receivers, event);
		
		TaskManager.create(String.format("%s", event), multicast);
	}
	
	private String method;
	private EventListener[] receivers;
	private EventObject event;
	
	private MulticastEvent() {}

	private void initial(String method,EventListener[] receivers,EventObject event) {
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
		MulticastEvent.returnObject(this);
	}
	
}
