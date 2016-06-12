package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author nieyu
 * 标示一个字段为long型主键
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Id {
    /**
     * 是否为自增长的主键，这个注释修饰的必须是long型字段
     * true : auto increasement
     */
    boolean auto() default false;

}