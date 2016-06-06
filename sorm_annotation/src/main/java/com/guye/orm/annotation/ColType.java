package com.guye.orm.annotation;

/**
 * 描述一个数据库字段类型
 *
 */
public enum ColType {

    NULL("NULL"), INTEGER("INTEGER"), REAL("REAL"), TEXT("TEXT"), BLOB("BLOB");//,MIXED("MIXED");
    
    public String value;
    
    private ColType(String v){
        value = v;
    }
}
