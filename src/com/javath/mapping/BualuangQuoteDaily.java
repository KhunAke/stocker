package com.javath.mapping;

// Generated Sep 10, 2014 9:53:57 AM by Hibernate Tools 4.0.0

/**
 * BualuangQuoteDaily generated by hbm2java
 */
public class BualuangQuoteDaily implements java.io.Serializable {

	private BualuangQuoteDailyId id;
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Long volume;
	private Double value;

	public BualuangQuoteDaily() {
	}

	public BualuangQuoteDaily(BualuangQuoteDailyId id) {
		this.id = id;
	}

	public BualuangQuoteDaily(BualuangQuoteDailyId id, Double open,
			Double high, Double low, Double close, Long volume, Double value) {
		this.id = id;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.value = value;
	}

	public BualuangQuoteDailyId getId() {
		return this.id;
	}

	public void setId(BualuangQuoteDailyId id) {
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
