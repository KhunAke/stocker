package com.javath.util;

public class ObjectException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public ObjectException(String message) {
		super(message);
	}
	public ObjectException(String format, java.lang.Object... value) {
		this(String.format(format, value));
	}
	public ObjectException(Throwable cause) {
		this(cause, cause.getMessage());
	}
	public ObjectException(Throwable cause, String message) {
		super(message, cause);
	}
	public ObjectException(Throwable cause, String format, java.lang.Object... value) {
		this(cause, String.format(format, value));
	}
	
	public boolean equalsCause(Class<?> classname) {
		return this.getCause().getClass().getCanonicalName()
				.equals(classname.getCanonicalName());
	}
}
