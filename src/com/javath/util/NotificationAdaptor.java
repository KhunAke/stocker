package com.javath.util;

import java.util.Date;
import java.util.EventListener;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.javath.logger.LOG;
import com.javath.trigger.MulticastEvent;
import com.javath.util.NotificationEvent.Status;

public class NotificationAdaptor implements NotificationSource {
	
	private final Set<NotificationListener> listeners;
	
	public NotificationAdaptor() {
		listeners = new HashSet<NotificationListener>();
	}
	
	@Override
	public boolean addListener(NotificationListener listener) {
		return listeners.add(listener);
	}
	@Override
	public boolean removeListener(NotificationListener listener) {
		return listeners.remove(listener);
	}
	public void notify(Status status, String message, Object... objects) {
		notify(status, String.format(message, objects));
	}
	public void notify(Status status, String message) {
		try {
			EventListener[] listeners = this.listeners.toArray(new EventListener[] {});
			NotificationEvent event = new NotificationEvent(this, status, message);
			System.out.printf("%s: %s%n", DateTime.timestamp(new Date()), event);
			if (listeners.length > 0 ) {
				MulticastEvent.send("notify", listeners, event);
			} 
		} catch (NoSuchElementException e) {
			LOG.SEVERE(e);
		} catch (IllegalStateException e) {
			LOG.SEVERE(e);
		} catch (Exception e) {
			LOG.SEVERE(e);
		}
	}

}
