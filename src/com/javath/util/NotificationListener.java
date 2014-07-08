package com.javath.util;

import java.util.EventListener;

public interface NotificationListener extends EventListener {
	public void notify(NotificationEvent event);
}
