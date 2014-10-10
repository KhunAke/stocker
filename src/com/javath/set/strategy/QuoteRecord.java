package com.javath.set.strategy;

public class QuoteRecord {
	
	private double price;
	private long volume;
	private int again;
	
	public double getPrice() {
		return price;
	}
	public long getVolume() {
		return volume;
	}
	public int getAgain() {
		return again;
	}
	
	public void set(double price) {
		this.price = price;
		this.volume = 0;
		this.again = -1;
	}
	public void add(long volume) {
		this.volume += volume;
		this.again += 1;
	}

}
