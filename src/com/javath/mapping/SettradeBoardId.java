package com.javath.mapping;

// Generated Jul 25, 2014 8:28:23 AM by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * SettradeBoardId generated by hbm2java
 */
public class SettradeBoardId implements java.io.Serializable {

	private String symbol;
	private Date date;

	public SettradeBoardId() {
	}

	public SettradeBoardId(String symbol, Date date) {
		this.symbol = symbol;
		this.date = date;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SettradeBoardId))
			return false;
		SettradeBoardId castOther = (SettradeBoardId) other;

		return ((this.getSymbol() == castOther.getSymbol()) || (this
				.getSymbol() != null && castOther.getSymbol() != null && this
				.getSymbol().equals(castOther.getSymbol())))
				&& ((this.getDate() == castOther.getDate()) || (this.getDate() != null
						&& castOther.getDate() != null && this.getDate()
						.equals(castOther.getDate())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getSymbol() == null ? 0 : this.getSymbol().hashCode());
		result = 37 * result
				+ (getDate() == null ? 0 : this.getDate().hashCode());
		return result;
	}

}
