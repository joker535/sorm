package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nieyu
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Table {
    
    /**
     * 表名，空则使用类名
     */
    String value() default "";

    /**
     * 是否使用生成代码方式。true：生成代码方式，false：反射方式
     */
    boolean genCode() default false;
}
