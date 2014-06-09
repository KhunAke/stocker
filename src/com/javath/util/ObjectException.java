package com.javath.util;

public class ObjectException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public ObjectException(String message) {
		super(message);
	}
	public ObjectException(String format, java.lang.Object... value) {
		this(String.format(format, value));
	}
	public ObjectException(String message, Throwable cause) {
		super(message, cause);
	}
	public ObjectException(Throwable cause) {
		this(cause.getMessage(), cause);
	} 

}
