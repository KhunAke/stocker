package com.javath.mapping;

// Generated Sep 23, 2014 1:12:15 PM by Hibernate Tools 4.0.0

/**
 * SetIndustryId generated by hbm2java
 */
public class SetIndustryId implements java.io.Serializable {

	private short marketId;
	private short industryId;

	public SetIndustryId() {
	}

	public SetIndustryId(short marketId, short industryId) {
		this.marketId = marketId;
		this.industryId = industryId;
	}

	public short getMarketId() {
		return this.marketId;
	}

	public void setMarketId(short marketId) {
		this.marketId = marketId;
	}

	public short getIndustryId() {
		return this.industryId;
	}

	public void setIndustryId(short industryId) {
		this.industryId = industryId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SetIndustryId))
			return false;
		SetIndustryId castOther = (SetIndustryId) other;

		return (this.getMarketId() == castOther.getMarketId())
				&& (this.getIndustryId() == castOther.getIndustryId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getMarketId();
		result = 37 * result + this.getIndustryId();
		return result;
	}

}
