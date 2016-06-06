package com.guye.orm.apt.type;

/**
 *
 */
public enum PkType {

    UNKNOWN("UNKNOWN"), ID("ID"), NAME("NAME"), COMPOSITE("COMPOSITE");
    
    public String value ;
    
    private PkType(String v){
        value = v;
    }

}