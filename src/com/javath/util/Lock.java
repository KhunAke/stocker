package com.javath.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Lock<Type> {
	
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private long owner = 0;
	private Type resource;
	
	public boolean acquire(boolean write) {
		if (write)
			if (owner == 0) {
				lock.writeLock().lock();
				owner = Thread.currentThread().getId();
				return true;
			} else {
				return false;
			}
		else {
			lock.readLock().lock();
			return true;
		}
	}	
	public boolean release(boolean write) {
		if (write)
			if (owner == Thread.currentThread().getId()) {
				owner = 0;
				lock.writeLock().unlock();
				return true;
			} else {
				return false;
			}
		else {
			lock.readLock().unlock();
			return true;
		}
	}
	
	public Type getResource() {
		if (owner == 0)
			return resource;
		else {
			acquire(false);
			try {
				return resource;
			} finally {
				release(false);
			}
		} 
	}
	public Lock<Type> setResource(Type resource) {
		if (owner == Thread.currentThread().getId()) {
			this.resource = resource;
			return this;
		} else
			throw new ObjectException("Current thread has not acquired");
	}
	
}
