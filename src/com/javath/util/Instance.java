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
			if (arguments.length == 0)
				return clazz.newInstance();
			else
				return forConstructor(clazz, arguments);
		} catch (ClassNotFoundException e) {
			return forMethod(classname.substring(0, classname.lastIndexOf('.')), 
					classname.substring(classname.lastIndexOf('.') + 1), arguments);
		} catch (InstantiationException e) {
			return forMethod(classname, "getInstance");
		} catch (IllegalAccessException e) {
			return forMethod(classname, "getInstance");
		}
	}
	private static Object forConstructor(Class<?> clazz, String... arguments) {
		boolean array = false;
		Class<?>[] type_arguments = null;
		Class<?>[] types = null;
		Constructor<?> constructor = null;
		while (true) {
			if (array) {
				types = new Class<?>[1];
				types[0] = String[].class;
			} else {
				types = new Class<?>[arguments.length];
				for (int index = 0; index < arguments.length; index++) {
					Object argument = arguments[index];
					types[index] = argument.getClass();
				}
				type_arguments = types;
			}
			try {
				constructor = clazz.getConstructor(types);
				break;
			} catch (NoSuchMethodException e) {
				if (!array) {
					array = true;
					continue;
				} else
					return forMethod(clazz, "getInstance", type_arguments, arguments);
			} catch (SecurityException e) {
				LOG.SEVERE(e);
			}
			break;
		}
		try {
			if (array)
				return constructor.newInstance((Object) arguments);
			else 
				return constructor.newInstance((Object[]) arguments);
		} catch (InstantiationException e) {
			LOG.SEVERE(e);
		} catch (IllegalAccessException e) {
			LOG.SEVERE(e);
		} catch (IllegalArgumentException e) {
			LOG.SEVERE(e);
		} catch (InvocationTargetException e) {
			LOG.SEVERE(e);
		}
		return null;
	}
	private static Object forMethod(String classname, String name, String... arguments) {
		try {
			Class<?> clazz = Class.forName(classname);
			if (arguments.length == 0)
				return forMethod(clazz, name, new Class<?>[] {});
			else {
				Class<?>[] types = new Class<?>[arguments.length];
				for (int index = 0; index < arguments.length; index++) {
					Object argument = arguments[index];
					types[index] = argument.getClass();
				}
				return forMethod(clazz, name, types, arguments);
			}		
		} catch (ClassNotFoundException e) {
			LOG.SEVERE(
					new ObjectException(e, String.format("Classloader \"%1$s\" and \"%1$s.%2$s\" not found", classname, name)));
		}
		return null;
	}
	private static Object forMethod(Class<?> classname, String name, Class<?>[] types, String... arguments) {
		Method method = null;
		boolean array = false;
		while (true) {
			if (array) {
				types = new Class<?>[1];
				types[0] = String[].class;
			}
			try {
				if (arguments.length == 0)
					method = classname.getMethod(name);
				else 
					method = classname.getMethod(name, types);
			} catch (NoSuchMethodException e) {
				if ((arguments.length != 0) && !(array)) {
					array = true;
					continue;
				} 
				LOG.SEVERE(e);
			} catch (SecurityException e) {
				LOG.SEVERE(e);
			}
			break;
		}
		try {
			if (array)
				return method.invoke(null, (Object) arguments);
			else 
				return method.invoke(null, (Object[]) arguments);
		} catch (IllegalAccessException e) {
			LOG.SEVERE(e);
		} catch (IllegalArgumentException e) {
			LOG.SEVERE(e);
		} catch (InvocationTargetException e) {
			LOG.SEVERE(e);
		}
		return null;
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
