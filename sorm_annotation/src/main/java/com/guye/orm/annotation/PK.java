package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明了一个 组合 的主键。
 * <p>
 * 本注解声明在某一个类上。
 * 
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PK {

    /**
     * 复合主键包含的字段名。
     */
    String[] value();

}