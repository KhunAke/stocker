package com.javath.mapping;
// Generated Aug 6, 2014 8:47:29 AM by Hibernate Tools 4.0.0



/**
 * SetIndustry generated by hbm2java
 */
public class SetIndustry  implements java.io.Serializable {


     private SetIndustryId id;
     private String nameTh;
     private String nameEn;

    public SetIndustry() {
    }

	
    public SetIndustry(SetIndustryId id) {
        this.id = id;
    }
    public SetIndustry(SetIndustryId id, String nameTh, String nameEn) {
       this.id = id;
       this.nameTh = nameTh;
       this.nameEn = nameEn;
    }
   
    public SetIndustryId getId() {
        return this.id;
    }
    
    public void setId(SetIndustryId id) {
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


