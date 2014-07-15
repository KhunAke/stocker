package com.javath.mapping;

// Generated Jul 15, 2014 1:21:50 PM by Hibernate Tools 4.0.0

/**
 * SetSector generated by hbm2java
 */
public class SetSector implements java.io.Serializable {

	private SetSectorId id;
	private String nameTh;
	private String nameEn;

	public SetSector() {
	}

	public SetSector(SetSectorId id) {
		this.id = id;
	}

	public SetSector(SetSectorId id, String nameTh, String nameEn) {
		this.id = id;
		this.nameTh = nameTh;
		this.nameEn = nameEn;
	}

	public SetSectorId getId() {
		return this.id;
	}

	public void setId(SetSectorId id) {
		this.id = id;
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

}