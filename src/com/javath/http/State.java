package com.javath.http;

import java.util.NoSuchElementException;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
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
				new BasePooledObjectFactory<State>() {
					@Override
					public State create() throws Exception {
						return new State();
					}
					/**
				     * Use the default PooledObject implementation.
				     */
					@Override
					public PooledObject<State> wrap(State state) {
						return new DefaultPooledObject<State>(state);
					}
					/**
				     * When an object is returned to the pool, clear the buffer.
				     */
				    @Override
				    public void passivateObject(PooledObject<State> pooled) {
				    	pooled.getObject().initial();
				    }
				    /**/
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
