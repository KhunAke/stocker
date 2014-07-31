package com.javath.mapping;

// Generated Jul 31, 2014 2:50:57 PM by Hibernate Tools 4.0.0

/**
 * SettradeMarket generated by hbm2java
 */
public class SettradeMarket implements java.io.Serializable {

	private SettradeMarketId id;
	private Double last;
	private Double changePrior;
	private Double high;
	private Double low;
	private Long volume;
	private Double value;

	public SettradeMarket() {
	}

	public SettradeMarket(SettradeMarketId id) {
		this.id = id;
	}

	public SettradeMarket(SettradeMarketId id, Double last, Double changePrior,
			Double high, Double low, Long volume, Double value) {
		this.id = id;
		this.last = last;
		this.changePrior = changePrior;
		this.high = high;
		this.low = low;
		this.volume = volume;
		this.value = value;
	}

	public SettradeMarketId getId() {
		return this.id;
	}

	public void setId(SettradeMarketId id) {
		this.id = id;
	}

	public Double getLast() {
		return this.last;
	}

	public void setLast(Double last) {
		this.last = last;
	}

	public Double getChangePrior() {
		return this.changePrior;
	}

	public void setChangePrior(Double changePrior) {
		this.changePrior = changePrior;
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
