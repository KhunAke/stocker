package com.javath.set;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;

import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.util.Instance;

public abstract class Broker extends Instance {

	// Key is username@classname
	private static Map<String,Broker> brokers = new HashMap<String,Broker>();
	public final static Broker dummy = new Broker() {
			@Override
			public boolean checkPassword(String password) {
				return true;
			}
			
			@Override
			protected CookieStore login(Browser browser) {
				return null;
			}
		};
		
	protected static Broker getInstance(String classname, String username) {
		return brokers.get(String.format("%s@%s", username, classname));
	}
	public static Broker getInstance(Class<? extends Broker> classname, String username, String password) {
		Broker result = getInstance(classname.getCanonicalName(), username);
		try {
			if (result.checkPassword(password))
				return result;
			else
				return dummy;
		} catch (NullPointerException e) {
			return dummy;
		}
	}
	protected static void putBroker(Broker broker, String username) {
		brokers.put(
				String.format("%s@%s", username, broker.getClass().getCanonicalName()), 
				broker);
	}
	
	protected Browser browser;
	protected Cookie cookie;
	
	public abstract boolean checkPassword(String password);
	protected void authentication(Browser browser, Cookie cookie) {
		if (cookie.acquire(true))
			try {
				if (browser.getCookie().equals(cookie.getCookieStore()))
					cookie.setCookieStore(login(browser));
				else
					browser.setCookie(cookie.getCookieStore());
			} finally {
				cookie.release(true);
			}
		else
			browser.setCookie(cookie.getCookieStore());
	}
	protected abstract CookieStore login(Browser browser);
	
}
