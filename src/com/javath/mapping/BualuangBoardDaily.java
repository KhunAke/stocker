package com.javath.mapping;

// Generated Jul 15, 2014 1:21:50 PM by Hibernate Tools 4.0.0

/**
 * BualuangBoardDaily generated by hbm2java
 */
public class BualuangBoardDaily implements java.io.Serializable {

	private BualuangBoardDailyId id;
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Long volume;
	private Double value;

	public BualuangBoardDaily() {
	}

	public BualuangBoardDaily(BualuangBoardDailyId id) {
		this.id = id;
	}

	public BualuangBoardDaily(BualuangBoardDailyId id, Double open,
			Double high, Double low, Double close, Long volume, Double value) {
		this.id = id;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.value = value;
	}

	public BualuangBoardDailyId getId() {
		return this.id;
	}

	public void setId(BualuangBoardDailyId id) {
		this.id = id;
	}

	public Double getOpen() {
		return this.open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return this.high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return this.low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return this.close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Long getVolume() {
		return this.volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Double getValue() {
		return this.value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
