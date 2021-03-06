package com.javath.mapping;

// Generated Sep 29, 2014 1:56:13 PM by Hibernate Tools 4.0.0

import java.util.Date;

/**
 * SetCompany generated by hbm2java
 */
public class SetCompany implements java.io.Serializable {

	private String symbol;
	private Short marketId;
	private Short industryId;
	private Short sectorId;
	private boolean set50;
	private boolean set100;
	private boolean setHd;
	private String nameTh;
	private String nameEn;
	private String website;
	private Date lastUpdate;

	public SetCompany() {
	}

	public SetCompany(String symbol, boolean set50, boolean set100,
			boolean setHd) {
		this.symbol = symbol;
		this.set50 = set50;
		this.set100 = set100;
		this.setHd = setHd;
	}

	public SetCompany(String symbol, Short marketId, Short industryId,
			Short sectorId, boolean set50, boolean set100, boolean setHd,
			String nameTh, String nameEn, String website, Date lastUpdate) {
		this.symbol = symbol;
		this.marketId = marketId;
		this.industryId = industryId;
		this.sectorId = sectorId;
		this.set50 = set50;
		this.set100 = set100;
		this.setHd = setHd;
		this.nameTh = nameTh;
		this.nameEn = nameEn;
		this.website = website;
		this.lastUpdate = lastUpdate;
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

	public boolean isSet50() {
		return this.set50;
	}

	public void setSet50(boolean set50) {
		this.set50 = set50;
	}

	public boolean isSet100() {
		return this.set100;
	}

	public void setSet100(boolean set100) {
		this.set100 = set100;
	}

	public boolean isSetHd() {
		return this.setHd;
	}

	public void setSetHd(boolean setHd) {
		this.setHd = setHd;
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

	public Date getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

}
