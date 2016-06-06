package com.guye.orm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.guye.orm.utils.Mirror;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * 描述了一个实体
 * 
 */
public interface Entity<T> {

    /**
     * @return 实体的 Java 类型
     */
    Class<T> getType();

    /**
     * @return 实体的 Java 类型
     */
    Mirror<T> getMirror();

    /**
     * 获取实体的表名
     * 
     * @return 实体表名
     */
    String getTableName();

    T bronObject();
    /**
     * 从结果集中生成一个实体实例
     * 
     * @param rs
     *            结果集
     * @param matcher
     *            字段匹配器。如果为null，则获取实体的全部字段
     * @return Java 对象
     */
     
     T getObject( Cursor rs ,T object);

    /**
     * 从一个记录中生成一个实体实例
     * 
     * @param rec
     *            结果集
     * 
     * @return Java 对象
     */
    T getObject(ContentValues rec,T object);

    ContentValues getValues(T t , String regex);
    
    /**
     * 根据实体的 Java 字段名获取一个实体字段对象
     * 
     * @param name
     *            实体字段的 Java 对象名
     * @return 实体字段
     */
    MappingField getField(String name);


    /**
     * 根据实体的数据库字段名获取一个实体字段对象
     * 
     * @param name
     *            实体字段数据库字段名
     * @return 实体字段
     */
    MappingField getColumn(String name);

    /**
     * @return 实体所有的映射字段
     */
    List<MappingField> getMappingFields();

    /**
     * 获取实体所有匹配上正则表达是的关联字段，如果正则表达是为 null，则表示获取全部关联字段
     * 
     * @param regex
     *            正则表达式
     * 
     * @return 实体所有匹配上正则表达是的关联字段
     */
    public List<LinkField> getOneLinkFields( String regex );
    
    public List<LinkField> getManyLinkFields( String regex );

    /**
     * 如果实体采用了复合主键，调用这个函数能返回所有的复合主键，顺序就是复合主键的顺序
     * <p>
     * 如果没有复合主键，那么将返回 null
     * 
     * @return 实体所复合主键字段
     */
    List<MappingField> getCompositePKFields();

    /**
     * @return 实体唯一字符类型主键
     */
    MappingField getNameField();

    /**
     * @return 实体唯一数字类型主键
     */
    MappingField getIdField();

    /**
     * 根据，"数字主键 > 字符主键 > 复合主键" 的优先顺序，返回主键列表
     * 
     * @return 实体的主键列表
     */
    List<MappingField> getPks();

    /**
     * @return 当前实体首选主键类型
     */
    PkType getPkType();

    boolean isComplete();
}