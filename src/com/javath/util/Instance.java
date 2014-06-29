package com.javath.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.javath.logger.LOG;

public class Instance {
	
	private static final String INIT = "com.javath.util.Instance";
	
	public static Object get(String classname, String... arguments) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(classname);
			return clazz.newInstance();
		} catch (ClassNotFoundException e) {
			return forMethod(classname.substring(0, classname.lastIndexOf('.')), 
					classname.substring(classname.lastIndexOf('.') + 1), arguments);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			if (arguments.length == 0)
				return forMethod(classname, "getInstance");
			else
				return forConstructor(clazz, arguments);
		}
	}
	private static Object forConstructor(Class<?> clazz, Object... arguments) {
		Object result = null;
		Class<?>[] types = new Class<?>[arguments.length];
		for (int index = 0; index < arguments.length; index++) {
			Object argument = arguments[index];
			types[index] = argument.getClass();
		}
		try {
			Constructor<?> constructor = clazz.getConstructor(types);
			return constructor.newInstance(arguments);
		} catch (NoSuchMethodException e) {
			try {
				Constructor<?> constructor = clazz.getConstructor(String[].class);
				return constructor.newInstance((Object) arguments);
			} catch (NoSuchMethodException ex) {
				LOG.SEVERE(ex);
			} catch (SecurityException ex) {
				LOG.SEVERE(ex);
			} catch (InstantiationException ex) {
				LOG.SEVERE(ex);
			} catch (IllegalAccessException ex) {
				LOG.SEVERE(ex);
			} catch (IllegalArgumentException ex) {
				LOG.SEVERE(ex);
			} catch (InvocationTargetException ex) {
				LOG.SEVERE(ex);
			}
		} catch (SecurityException e) {
			LOG.SEVERE(e);
		} catch (InstantiationException e) {
			LOG.SEVERE(e);
		} catch (IllegalAccessException e) {
			LOG.SEVERE(e);
		} catch (IllegalArgumentException e) {
			LOG.SEVERE(e);
		} catch (InvocationTargetException e) {
			LOG.SEVERE(e);
		}
		return result;
	}
	private static Object forMethod(String classname, String method_name, String... arguments) {
		Object result = null;
		try {
			Class<?> clazz = Class.forName(classname);
			if (arguments.length == 0) {
				Method method = clazz.getMethod(method_name);
				return method.invoke(null);
			} else {
				Class<?>[] types = new Class<?>[arguments.length];
				for (int index = 0; index < arguments.length; index++) {
					Object argument = arguments[index];
					types[index] = argument.getClass();
				}
				Method method = clazz.getMethod(method_name, types);
				return method.invoke(null, (Object[]) arguments);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	private static Object forMethod(String clazz, String method_name, Class<?>[] type, Object... arguments) {
		Object result = null;
		return result;
	}
	
	protected final Logger logger;
	private final int logger_level;
	protected final int InstanceId;
	
	private String classname;
	private String packagename;
	
	public Instance() {
		InstanceId = hashCode();

		try {
			StackTraceElement[] stack = new Throwable().getStackTrace();
			int index = 0;
			//for (index = stack.length - 1; index > 0; index--) {
			String supperclass = INIT;
			
			for (index = 0; index < stack.length; index++) {
				classname = stack[index].getClassName();
				if (!classname.equals(INIT))
					if (stack[index].getMethodName().equals("<init>")) {
						Class<?> clazz = Class.forName(classname);
						if (clazz.getSuperclass().getCanonicalName().equals(supperclass))
							supperclass = classname;
						else
							break;
					}
			}
			classname = supperclass;
			packagename = Class.forName(classname).getPackage().getName();
		} catch (ClassNotFoundException e) {
			classname = null;
			SEVERE(e);
		} finally {
			if (classname == null)
				logger = LOG.getLogger();
			else
				logger = Logger.getLogger(classname);
			logger_level = LOG.getLoggerLevel(logger);
		}
		FINE("Create instance of \"%s\"", classname);
	}
	
	public int getInstanceId() {
		return InstanceId;
	}
	public String getClassName() {
		return classname;
	}
	public String getPackageName() {
		return packagename;
	}
	
	private boolean checkLogging(Level level) {
		if (level.intValue() >= logger_level)
			return true;
		else
			return false;
	}
	protected void SEVERE(Locale locale, String format, Object... args ){
		if (checkLogging(Level.SEVERE))
			SEVERE(String.format(locale, format, args));
	}
	protected void SEVERE(String format, Object... args ){
		if (checkLogging(Level.SEVERE))
			SEVERE(String.format(format, args));
	}
	protected void SEVERE(String message) {
		if (checkLogging(Level.SEVERE))
			log(Level.SEVERE, message);
	}
	protected void WARNING(Locale locale, String format, Object... args ){
		if (checkLogging(Level.WARNING))
			WARNING(String.format(locale, format, args));
	}
	protected void WARNING(String format, Object... args ){
		if (checkLogging(Level.WARNING))
			WARNING(String.format(format, args));
	}
	protected void WARNING(String message) {
		if (checkLogging(Level.WARNING))
			log(Level.WARNING, message);
	}
	protected void INFO(Locale locale, String format, Object... args ){
		if (checkLogging(Level.INFO))
			INFO(String.format(locale, format, args));
	}
	protected void INFO(String format, Object... args ){
		if (checkLogging(Level.INFO))
			INFO(String.format(format, args));
	}
	protected void INFO(String message) {
		if (checkLogging(Level.INFO))
			log(Level.INFO, message);
	}	
	protected void CONFIG(Locale locale, String format, Object... args ){
		if (checkLogging(Level.CONFIG))
			CONFIG(String.format(locale, format, args));
	}
	protected void CONFIG(String format, Object... args ){
		if (checkLogging(Level.CONFIG))
			CONFIG(String.format(format, args));
	}
	protected void CONFIG(String message) {
		if (checkLogging(Level.CONFIG))
			log(Level.CONFIG, message);
	}
	protected void FINE(Locale locale, String format, Object... args ){
		if (checkLogging(Level.FINE))
			FINE(String.format(locale, format, args));
	}
	protected void FINE(String format, Object... args ){
		if (checkLogging(Level.FINE))
			FINE(String.format(format, args));
	}
	protected void FINE(String message) {
		if (checkLogging(Level.FINE))
			log(Level.FINE, message);
	}
	protected void FINER(Locale locale, String format, Object... args ){
		if (checkLogging(Level.FINER))
			FINER(String.format(locale, format, args));
	}
	protected void FINER(String format, Object... args ){
		if (checkLogging(Level.FINER))
			FINER(String.format(format, args));
	}
	protected void FINER(String message) {
		if (checkLogging(Level.FINER))
			log(Level.FINER, message);
	}
	protected void FINEST(Locale locale, String format, Object... args ){
		if (checkLogging(Level.FINEST))
			FINEST(String.format(locale, format, args));
	}
	protected void FINEST(String format, Object... args ){
		if (checkLogging(Level.FINEST))
			FINEST(String.format(format, args));
	}
	protected void FINEST(String message) {
		if (checkLogging(Level.FINEST))
			log(Level.FINEST, message);
	}
	private void log(Level level,String message) {
		StackTraceElement[] stack = new Throwable().getStackTrace();
		String methodname = null;
		for (int index = 0; index < stack.length; index++) {
			String classname = stack[index].getClassName();
			if (this.classname.equals(classname)) {
				methodname = stack[index].getMethodName();
				break;
			}
		}
		logger.logp(level, classname, methodname, 
				String.format("[@%d] %s", InstanceId, message));
	}

	protected void SEVERE(Throwable throwable){
		if (checkLogging(Level.SEVERE))
			log(Level.SEVERE, throwable);
	}
	protected void WARNING(Throwable throwable){
		if (checkLogging(Level.WARNING))
			log(Level.WARNING, throwable);
	}
	protected void INFO(Throwable throwable){
		if (checkLogging(Level.INFO))
			log(Level.INFO, throwable);
	}
	protected void CONFIG(Throwable throwable){
		if (checkLogging(Level.CONFIG))
			log(Level.CONFIG, throwable);
	}
	protected void FINE(Throwable throwable){
		if (checkLogging(Level.FINE))
			log(Level.FINE, throwable);
	}
	protected void FINER(Throwable throwable){
		if (checkLogging(Level.FINER))
			log(Level.FINER, throwable);
	}
	protected void FINEST(Throwable throwable){
		if (checkLogging(Level.FINEST))
			log(Level.FINEST, throwable);
	}
	private void log(Level level, Throwable throwable) {
		StackTraceElement[] stack = throwable.getStackTrace();
		String methodname = null;
		String filename = null;
		int linenumber = 0;
		for (int index = 0; index < stack.length; index++) {
			String classname = stack[index].getClassName();
			if (this.classname.equals(classname)) {
				methodname = stack[index].getMethodName();
				filename = stack[index].getFileName();
				linenumber = stack[index].getLineNumber();
				break;
			}
		}
		logger.logp(level, classname, methodname, 
				String.format("[@%d] %s: %s (%s:%d)", InstanceId, 
						throwable.getClass().getCanonicalName(), throwable.getMessage(), 
						filename, linenumber));
		if (Assign.debug)
			synchronized (System.err) {
				System.err.printf("%s [@%d] ", DateTime.timestamp(new Date()), InstanceId);
				throwable.printStackTrace(System.err);
			}
	}

	@Override
	public String toString() {
		return String.format("%s[InstanceId=%d]",
				classname, InstanceId);
	}

	@Override
	protected void finalize() throws Throwable {
		FINE("Destroy instance of \"%s\"", classname);
		super.finalize();
	}
	
}
