package com.javath.http;

import java.util.NoSuchElementException;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import com.javath.util.Instance;
import com.javath.util.Lock;
import com.javath.util.ObjectException;

public class State extends Instance {
	private final static ObjectPool<State> pool_state;
	
	static {
		pool_state = initialPoolState();
	}
	
	private final static ObjectPool<State> initialPoolState() {
		return new GenericObjectPool<State>(
				new PoolableObjectFactory<State>() {
					@Override
					public State makeObject() 
							throws Exception {
						return new State();
					}
					@Override
					public void activateObject(State state) 
							throws Exception {
						
					}
					@Override
					public void passivateObject(State state) 
							throws Exception {
						state.initial();
					}
					@Override
					public boolean validateObject(State state) {
						return true;
					}
					@Override
					public void destroyObject(State state) 
							throws Exception {}
				});
	}
	
	public static State borrowObject() {
		try {
			return pool_state.borrowObject();
		} catch (NoSuchElementException e) {
			throw new ObjectException(e);
		} catch (IllegalStateException e) {
			throw new ObjectException(e);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}	
	public static void returnObject(State state) {
		try {
			pool_state.returnObject(state);
		} catch (Exception e) {
			throw new ObjectException(e);
		}
	}
	
	private Lock<CookieStore> cookie;
	
	private State() {
		cookie = new Lock<CookieStore>();
		cookie.acquire(true);
		try {
			cookie.setResource(new BasicCookieStore());
		} finally {
			cookie.release(true);
		}
	}
	private void initial() {
		cookie.acquire(true);
		try {
			cookie.getResource().clear();
		} finally {
			cookie.release(true);
		}
	}
	
	public boolean acquire(boolean write) {
		return cookie.acquire(write);
	}
	public boolean release(boolean write) {
		return cookie.release(write);
	}
	
	public CookieStore getCookieStore() {
		return cookie.getResource();
	}
	public State setCookieStore(CookieStore cookie) {
		this.cookie.setResource(cookie);
		return this;
	}
}
