package com.javath.mapping;

// Generated Sep 29, 2014 1:56:13 PM by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * SettradeMarketId generated by hbm2java
 */
public class SettradeMarketId implements java.io.Serializable {

	private String name;
	private Date date;

	public SettradeMarketId() {
	}

	public SettradeMarketId(String name, Date date) {
		this.name = name;
		this.date = date;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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
		if (!(other instanceof SettradeMarketId))
			return false;
		SettradeMarketId castOther = (SettradeMarketId) other;

		return ((this.getName() == castOther.getName()) || (this.getName() != null
				&& castOther.getName() != null && this.getName().equals(
				castOther.getName())))
				&& ((this.getDate() == castOther.getDate()) || (this.getDate() != null
						&& castOther.getDate() != null && this.getDate()
						.equals(castOther.getDate())));
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result
				+ (getName() == null ? 0 : this.getName().hashCode());
		result = 37 * result
				+ (getDate() == null ? 0 : this.getDate().hashCode());
		return result;
	}

}
