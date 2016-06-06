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
public @interface Column {
    String value() default "";
    boolean useGetAndSet() default true;
    boolean isNotNull() default false;
}
