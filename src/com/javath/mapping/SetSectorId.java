package com.javath.mapping;

// Generated Jul 15, 2014 1:21:50 PM by Hibernate Tools 4.0.0

/**
 * SetSectorId generated by hbm2java
 */
public class SetSectorId implements java.io.Serializable {

	private short marketId;
	private short industryId;
	private short sectorId;

	public SetSectorId() {
	}

	public SetSectorId(short marketId, short industryId, short sectorId) {
		this.marketId = marketId;
		this.industryId = industryId;
		this.sectorId = sectorId;
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

	public short getSectorId() {
		return this.sectorId;
	}

	public void setSectorId(short sectorId) {
		this.sectorId = sectorId;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SetSectorId))
			return false;
		SetSectorId castOther = (SetSectorId) other;

		return (this.getMarketId() == castOther.getMarketId())
				&& (this.getIndustryId() == castOther.getIndustryId())
				&& (this.getSectorId() == castOther.getSectorId());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getMarketId();
		result = 37 * result + this.getIndustryId();
		result = 37 * result + this.getSectorId();
		return result;
	}

}
