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
@Target({ ElementType.FIELD })
public @interface Many {

    /**
     * 关联类
     */
    Class<?> target();

    /**
     * target类的关联属性名
     */
    String field();

    /**
     * 当前类的一个属性名,缺省情况下,按参考字段名{@link #field()}的类型选取@Id或者@Name等主键字段
     */
    String key() default "";

}