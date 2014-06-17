package com.javath.logger;

import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.javath.util.Assign;
import com.javath.util.DateTime;
import com.javath.util.ObjectException;

public class LOG {
	
	private static final Logger logger;
	private static final int logger_level;
	
	static {
		//logger = Logger.getAnonymousLogger(); 
		logger = Logger.getLogger(LOG.class.getCanonicalName());
		logger_level = getLoggerLevel(logger);
	}
	
	public static Logger getLogger() {
		return logger;
	}
 	public static int getLoggerLevel(Logger logger) {
		Logger log = logger;
		while (log.getLevel() == null)
			log = log.getParent();
		return log.getLevel().intValue();
	}
	private static boolean checkLogging(Level level) {
		if (level.intValue() >= logger_level)
			return true;
		else
			return false;
	}

	public static void SEVERE(Locale locale, String format, Object... args ){
		if (checkLogging(Level.SEVERE))
			SEVERE(String.format(locale, format, args));
	}
	public static void SEVERE(String format, Object... args ){
		if (checkLogging(Level.SEVERE))
			SEVERE(String.format(format, args));
	}
	public static void SEVERE(String message) {
		if (checkLogging(Level.SEVERE))
			log(Level.SEVERE, message);
	}
	public static void WARNING(Locale locale, String format, Object... args ){
		if (checkLogging(Level.WARNING))
			WARNING(String.format(locale, format, args));
	}
	public static void WARNING(String format, Object... args ){
		if (checkLogging(Level.WARNING))
			WARNING(String.format(format, args));
	}
	public static void WARNING(String message) {
		if (checkLogging(Level.WARNING))
			log(Level.WARNING, message);
	}
	public static void INFO(Locale locale, String format, Object... args ){
		if (checkLogging(Level.INFO))
			INFO(String.format(locale, format, args));
	}
	public static void INFO(String format, Object... args ){
		if (checkLogging(Level.INFO))
			INFO(String.format(format, args));
	}
	public static void INFO(String message) {
		if (checkLogging(Level.INFO))
			log(Level.INFO, message);
	}	
	public static void CONFIG(Locale locale, String format, Object... args ){
		if (checkLogging(Level.CONFIG))
			CONFIG(String.format(locale, format, args));
	}
	public static void CONFIG(String format, Object... args ){
		if (checkLogging(Level.CONFIG))
			CONFIG(String.format(format, args));
	}
	public static void CONFIG(String message) {
		if (checkLogging(Level.CONFIG))
			log(Level.CONFIG, message);
	}
	public static void FINE(Locale locale, String format, Object... args ){
		if (checkLogging(Level.FINE))
			FINE(String.format(locale, format, args));
	}
	public static void FINE(String format, Object... args ){
		if (checkLogging(Level.FINE))
			FINE(String.format(format, args));
	}
	public static void FINE(String message) {
		if (checkLogging(Level.FINE))
			log(Level.FINE, message);
	}
	public static void FINER(Locale locale, String format, Object... args ){
		if (checkLogging(Level.FINER))
			FINER(String.format(locale, format, args));
	}
	public static void FINER(String format, Object... args ){
		if (checkLogging(Level.FINER))
			FINER(String.format(format, args));
	}
	public static void FINER(String message) {
		if (checkLogging(Level.FINER))
			log(Level.FINER, message);
	}
	public static void FINEST(Locale locale, String format, Object... args ){
		if (checkLogging(Level.FINEST))
			FINEST(String.format(locale, format, args));
	}
	public static void FINEST(String format, Object... args ){
		if (checkLogging(Level.FINEST))
			FINEST(String.format(format, args));
	}
	public static void FINEST(String message) {
		if (checkLogging(Level.FINEST))
			log(Level.FINEST, message);
	}
	private static void log(Level level,String message) {
		Throwable throwable = new ObjectException(message);
		StackTraceElement[] stack = throwable.getStackTrace();
		int call = 0;
		for (int index = 0 ; index < stack.length; index++) {
			if (!stack[index].getClassName().equals(LOG.class.getCanonicalName())) {
				call = index;
				break;
			}
		}
		String classname =  stack[call].getClassName();
		String methodname = stack[call].getMethodName();
		String filename = stack[call].getFileName();
		int linenumber = stack[call].getLineNumber();
		logger.logp(level, classname, methodname, 
				String.format("%s: %s (%s:%d)", 
						throwable.getClass().getCanonicalName(), throwable.getMessage(), 
						filename, linenumber));
		if (Assign.debug)
			synchronized (System.err) {
				System.err.printf("%s Exception in thread \"%s\" %s: %s\n", 
						DateTime.timestamp(new Date()), Thread.currentThread().getName(),
						throwable.getClass().getCanonicalName(), throwable.getMessage());
				for (int index = call; index < stack.length; index++) {
					if (stack[index].getFileName() != null)
						System.err.printf("\tat %s.%s(%s:%d)\n", 
								stack[index].getClassName(),
								stack[index].getMethodName(),
								stack[index].getFileName(),
								stack[index].getLineNumber());
					else 
						if (stack[index].getLineNumber() == -1)
							System.err.printf("\tat %s.%s(Unknown Source)\n", 
									stack[index].getClassName(),
									stack[index].getMethodName());
						else if (stack[index].getLineNumber() == -2)
							System.err.printf("\tat %s.%s(Native Method)\n", 
									stack[index].getClassName(),
									stack[index].getMethodName());
						else
							System.err.printf("\tat %s.%s\n", 
									stack[index].getClassName(),
									stack[index].getMethodName());
				}
			}
	}
	
	public static void SEVERE(Throwable throwable){
		if (checkLogging(Level.SEVERE))
			log(Level.SEVERE, throwable);
	}
	public static void WARNING(Throwable throwable){
		if (checkLogging(Level.WARNING))
			log(Level.WARNING, throwable);
	}
	public static void INFO(Throwable throwable){
		if (checkLogging(Level.INFO))
			log(Level.INFO, throwable);
	}
	public static void CONFIG(Throwable throwable){
		if (checkLogging(Level.CONFIG))
			log(Level.CONFIG, throwable);
	}
	public static void FINE(Throwable throwable){
		if (checkLogging(Level.FINE))
			log(Level.FINE, throwable);
	}
	public static void FINER(Throwable throwable){
		if (checkLogging(Level.FINER))
			log(Level.FINER, throwable);
	}
	public static void FINEST(Throwable throwable){
		if (checkLogging(Level.FINEST))
			log(Level.FINEST, throwable);
	}
	private static void log(Level level, Throwable throwable) {
		StackTraceElement[] stack = throwable.getStackTrace();
		String classname =  stack[0].getClassName();
		String methodname = stack[0].getMethodName();
		String filename = stack[0].getFileName();
		int linenumber = stack[0].getLineNumber();
		logger.logp(level, classname, methodname, 
				String.format("%s: %s (%s:%d)", 
						throwable.getClass().getCanonicalName(), throwable.getMessage(), 
						filename, linenumber));
		if (Assign.debug)
			synchronized (System.err) {
				System.err.printf("%s Exception in thread \"%s\" ", 
						DateTime.timestamp(new Date()), Thread.currentThread().getName());
				throwable.printStackTrace(System.err);
			}
	}

}
