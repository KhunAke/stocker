package com.javath.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

public class TaskManager {
	
	// Method name : create
	public final static Thread create(Runnable runnable) {
		long timestamp = new Date().getTime(); 
		String name = String.format("%s[timestamp=%d]", 
				runnable.getClass().getCanonicalName(), timestamp);
		return create(name, runnable);
	}
	public final static Thread create(String name, Runnable runnable) {
		Thread thread = new Thread(runnable, name);
		thread.start();
		return thread;
	}
	public final static Thread create(java.lang.Object object, String methodName, java.lang.Object... arguments) {
		long timestamp = new Date().getTime(); 
		String name = String.format("%s.%s(timestamp=%d)"
				, object.getClass().getCanonicalName(), methodName, timestamp);
		return create(name, object, methodName, arguments);
	}
	public final static Thread create(String name, java.lang.Object object, String methodName, java.lang.Object... arguments) {
	
		Thread thread = new Thread(new Runnable() {
			private java.lang.Object object;
			private Method method;
			//private String methodName;
			private java.lang.Object[] arguments;
			
			public Runnable setExecute(java.lang.Object object, String methodName, java.lang.Object... arguments) {
				try {
					this.object = object;
					this.arguments = arguments;
					Class<?>[] types = new Class<?>[arguments.length];
					for (int index = 0; index < arguments.length; index++) {
						Object argument = arguments[index];
						types[index] = argument.getClass();
					}
					this.method = object.getClass().getMethod(methodName, types);
				} catch (NoSuchMethodException e) {
					throw new ObjectException(e);
				} catch (SecurityException e) {
					throw new ObjectException(e);
				}
				return this;
			}
			
			public void run() {
				try {
					method.invoke(object, arguments);
				} catch (IllegalAccessException e) {
					throw new ObjectException(e);
				} catch (IllegalArgumentException e) {
					throw new ObjectException(e);
				} catch (InvocationTargetException e) {
					throw new ObjectException(e);
				} 
			}
			
		}.setExecute(object, methodName, arguments), name);
		thread.start();
		return thread;
	}
	
}
