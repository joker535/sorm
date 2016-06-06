package com.guye.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明了一个 组合 的主键。
 * <p>
 * 本注解声明在某一个 POJO 类上，例如：
 * 
 * <pre>
 * &#064;Table("t_abc")
 * &#064;PK({"id", "type"})
 * public class Abc{
 * ...
 * </pre>
 * <p>
 * 当然，你可以通过这个注解来替代 '@Id' 和 '@Name'，当你给出的字段只有一个的时候
 * <ul>
 * <li>整数型字段，将代表 '@Id'
 * <li>字符型字段，将代表 '@Name'
 * </ul>
 * 在 POJO 中，你可以同时声明 '@Id'，'@Name'以及 '@Pk'，但是 '@Id' 和 '@Name' 更优先
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PK {

    String[] value();
    
    /**
     * 仅建表时使用
     */
    String name() default "";

}