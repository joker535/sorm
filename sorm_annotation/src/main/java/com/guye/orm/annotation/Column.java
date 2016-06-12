package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nieyu
 * 列注释，需要存储到数据库中的字段必须添加这个注释
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD})
public @interface Column {
    /**
     * 对应存储到数据库的列名，不指定则用字段名
     */
    String value() default "";
    /**
     * 是否优先使用get和set方法，true表示优先使用get，set方法，没有get，set方法使用反射方式获取字段值。
     *注意 在生成代码方式下如果没有get和set方法，并且字段为private或者protected 会直接报错。
     */
    boolean useGetAndSet() default true;
    /**
     * 是否允许数据库字段为空。 
     */
    boolean isNotNull() default false;
}
