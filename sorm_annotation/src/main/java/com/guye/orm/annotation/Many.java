package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nieyu
 * 标示一个字段为一对多映射。一对多映射指当前表主键对应目标表中的某一字段。所以当前表必须包含一个long型或者字符型主键。不支持复合主键。
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

}