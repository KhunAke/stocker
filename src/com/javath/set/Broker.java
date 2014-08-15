package com.javath.set;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;

import com.javath.http.Browser;
import com.javath.http.Cookie;
import com.javath.util.Assign;
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
		
	protected static Broker getInstance(Class<? extends Broker> classname, String username) {
		return brokers.get(String.format("%s@%s", username, classname.getCanonicalName()));
	}
	public static Broker getInstance(Class<? extends Broker> classname, String username, String password) {
		Broker result = getInstance(classname, username);
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
	
	protected Cookie cookie;
	
	public abstract boolean checkPassword(String password);
	protected CookieStore authentication(Cookie cookie) {
		Browser browser = (Browser) Assign.borrowObject(Browser.class);
		try {
			if (cookie.acquire(true))
				try {
					browser.setCookie(cookie.getCookieStore());
					cookie.setCookieStore(login(browser));
				} finally {
					cookie.release(true);
				}
			return cookie.getCookieStore();
		} finally {
			Assign.returnObject(browser);
		}
	}
	protected abstract CookieStore login(Browser browser);
	
}
