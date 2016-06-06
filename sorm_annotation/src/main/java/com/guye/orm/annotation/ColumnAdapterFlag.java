package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by nieyu on 16/5/19.
 */
@Retention(RetentionPolicy.CLASS)
@Inherited
@Target(ElementType.TYPE)
public @interface ColumnAdapterFlag {
}
