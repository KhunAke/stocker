package com.javath.mapping;

// Generated Sep 23, 2014 1:12:15 PM by Hibernate Tools 4.0.0

/**
 * SetMarket generated by hbm2java
 */
public class SetMarket implements java.io.Serializable {

	private short marketId;
	private String name;

	public SetMarket() {
	}

	public SetMarket(short marketId) {
		this.marketId = marketId;
	}

	public SetMarket(short marketId, String name) {
		this.marketId = marketId;
		this.name = name;
	}

	public short getMarketId() {
		return this.marketId;
	}

	public void setMarketId(short marketId) {
		this.marketId = marketId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
