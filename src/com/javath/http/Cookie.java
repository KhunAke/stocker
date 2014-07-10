package com.javath.http;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import com.javath.util.Instance;
import com.javath.util.Lock;

public class Cookie extends Instance {
	
	private Lock<CookieStore> lock;
	
	public Cookie() {
		lock = new Lock<CookieStore>();
		lock.acquire(true);
		try {
			lock.setResource(new BasicCookieStore());
		} finally {
			lock.release(true);
		}
	}

	public boolean acquire(boolean write) {
		return lock.acquire(write);
	}
	public boolean release(boolean write) {
		return lock.release(write);
	}
	
	public CookieStore getCookieStore() {
		return lock.getResource();
	}
	public Cookie setCookieStore(CookieStore cookie) {
		this.lock.setResource(cookie);
		return this;
	}

}
