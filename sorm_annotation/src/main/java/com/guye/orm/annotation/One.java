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
@Target({ ElementType.FIELD})
public @interface One {

    /**
     * 关联类
     */
    Class<?> target();

    /**
     * 关联属性名
     */
    String field();

}