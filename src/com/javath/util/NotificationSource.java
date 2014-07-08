package com.javath.util;

public interface NotificationSource {
	public boolean addListener(NotificationListener listener);
	public boolean removeListener(NotificationListener listener);
}
