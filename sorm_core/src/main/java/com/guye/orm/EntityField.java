package com.guye.orm;

import java.lang.reflect.Type;

public interface EntityField {

    /**
     * @return 获取该字段 Java 对象的名称
     */
    String getName();

    /**
     * @return 获取该字段 Java 对象的类型
     */
    Type getType();

    /**
     * @return 获取该字段 Java 对象的类型
     */
    Class<?> getTypeClass();

    /**
     * 为当前实体字段注入值，优先通过 setter 注入
     * 
     * @param obj
     *            被设值对象
     * @param value
     *            值
     */
    void setValue(Object obj, Object value);

    /**
     * 从 Java 对象中获取一个值
     * 
     * @param obj
     *            Java 对象
     * @return 字段的值
     */
    Object getValue(Object obj);
    
}
