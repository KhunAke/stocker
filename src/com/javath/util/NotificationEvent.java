package com.javath.util;

import java.util.EventObject;

public class NotificationEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	public enum Status {
		UNKNOW,
		PROCESS,
		PENDING,
		DONE,
		FAIL,
		SUCCESS
	}
	
	private Status status;
	private String message;

	public NotificationEvent(Object source, Status status, String message, Object... objects) {
		this(source, status, String.format(message, objects));
	}
	public NotificationEvent(Object source, Status status, String message) {
		super(source);
		this.status = status;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public Status getStatus() {
		return status;
	}
	public String toString() {
		return String.format("%2$s; %1$s", status, message);
	}

}
