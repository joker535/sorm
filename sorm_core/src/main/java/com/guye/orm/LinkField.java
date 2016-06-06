package com.guye.orm;

import com.guye.orm.impl.RecordAdapter;


/**
 * 这个接口封装了不同映射关系行为的不同
 * 
 */
public interface LinkField extends EntityField {

    int ONE = 1;
    int MANY = 2;
    /**
     * @return 映射的类型
     */
    int getLinkType();

    /**
     * 根据给定的宿主对象，以及自身记录的映射关系，生成一个获取映射对象的约束条件
     * <ul>
     * <li>`@One` 根据宿主对象引用字段值生成映射对象的条件语句</li>
     * <li>`@Many` 根据宿主对象主键值生成映射对象的条件语句</li>
     * </ul>
     * 
     * @param host
     *            宿主对象
     * 
     * @return POJO 语句的条件元素
     */
    Condition createCondition(Object host);

    /**
     * @return 链接的目标实体
     */
    RecordAdapter<?> getLinkedEntity();

    Class<?> getTargetType() ;
}
