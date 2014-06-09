package com.javath.logger;

import java.util.Date;

import com.javath.util.DateTime;
import com.javath.util.Instance;

public class LogMessage {
	
	private final Instance instance;
	private String message;
	
	public LogMessage(Instance instance) {
		this.instance = instance;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	public void setMessage(String format, Object... args) {
		setMessage(String.format(format, args));
	}
	public void setMessage(Throwable throwable) {
		setMessage("%s: %s",throwable.getClass().getCanonicalName(), throwable.getMessage());
		System.err.printf("%s ", DateTime.timestamp(new Date()));
		throwable.printStackTrace(System.err);
	}
	
	public String toString() {
		return String.format("[@%d] %s", 
				instance.getInstanceId(), message);
	}
}
