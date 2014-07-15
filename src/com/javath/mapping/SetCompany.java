package com.javath.mapping;

// Generated Jul 15, 2014 1:21:50 PM by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * SetCompany generated by hbm2java
 */
public class SetCompany implements java.io.Serializable {

	private String symbol;
	private Short marketId;
	private Short industryId;
	private Short sectorId;
	private String nameTh;
	private String nameEn;
	private String website;
	private Date update;

	public SetCompany() {
	}

	public SetCompany(String symbol) {
		this.symbol = symbol;
	}

	public SetCompany(String symbol, Short marketId, Short industryId,
			Short sectorId, String nameTh, String nameEn, String website,
			Date update) {
		this.symbol = symbol;
		this.marketId = marketId;
		this.industryId = industryId;
		this.sectorId = sectorId;
		this.nameTh = nameTh;
		this.nameEn = nameEn;
		this.website = website;
		this.update = update;
	}

	public String getSymbol() {
		return this.symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Short getMarketId() {
		return this.marketId;
	}

	public void setMarketId(Short marketId) {
		this.marketId = marketId;
	}

	public Short getIndustryId() {
		return this.industryId;
	}

	public void setIndustryId(Short industryId) {
		this.industryId = industryId;
	}

	public Short getSectorId() {
		return this.sectorId;
	}

	public void setSectorId(Short sectorId) {
		this.sectorId = sectorId;
	}

	public String getNameTh() {
		return this.nameTh;
	}

	public void setNameTh(String nameTh) {
		this.nameTh = nameTh;
	}

	public String getNameEn() {
		return this.nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public String getWebsite() {
		return this.website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public Date getUpdate() {
		return this.update;
	}

	public void setUpdate(Date update) {
		this.update = update;
	}

}
