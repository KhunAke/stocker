package com.javath.util;

import java.util.EventObject;

public class NotificationEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	public enum NoteStatus {
		UNKNOW,
		PROCESS,
		PENDING,
		DONE,
		FAIL,
		SUCCESS
	}
	
	private NoteStatus status;
	private String message;

	public NotificationEvent(Object source, NoteStatus status, String message, Object... objects) {
		this(source, status, String.format(message, objects));
	}
	public NotificationEvent(Object source, NoteStatus status, String message) {
		super(source);
		this.status = status;
		this.message = message;
	}
	
	public boolean isClass(Class<?> classname) {
		Object source = getSource();
		return source.getClass().getCanonicalName()
				.equals(classname.getCanonicalName());
	}
	public boolean isObject(Object object) {
		Object source = getSource();
		return source.equals(object);
	}
	
 	public String getMessage() {
		return message;
	}
	public NoteStatus getStatus() {
		return status;
	}
	
	public String toString() {
		return String.format("%2$s; %1$s", status, message);
	}

}
