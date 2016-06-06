package com.guye.orm;

import java.lang.reflect.Field;

import com.guye.orm.annotation.ColType;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 这个接口描述了一个数据库字段与Java字段的映射关系
 * 
 */
public interface MappingField extends EntityField {

	/**
	 * 通过 Record 为映射字段注入值
	 * 
	 * @param obj
	 *            被注入对象
	 * @param rec
	 *            结果集
	 * @param prefix TODO
	 */
	void injectValue(Object obj, ContentValues rec);

	/**
	 * 通过 resultSet 为映射字段注入值
	 * 
	 * @param obj
	 *            被注入对象
	 * @param rs
	 *            结果集
	 * @param prefix TODO
	 */
	void injectValue(Object obj, Cursor rs);
	
	/**
	 * 通过对象获取字段值；
	 * 
	 * @param obj
	 *            被注入对象
	 * @param rs
	 *            结果集
	 * @param prefix TODO
	 */
	void enjectValue( Object obj,  ContentValues rec );

	/**
	 * @return 数据库中的字段名
	 */
	String getColumnName();

	/**
	 * @return 数据库中的字段类型
	 */
	ColType getColumnType();
	
	/**
	 * @return 序列化这个字段的类对象
	 */
	<T> ColumnAdapter<T> getAdaptor();

	/**
	 * 设置字段在数据库中的类型
	 * 
	 * @param colType
	 *            数据库字段的类型
	 */
	void setColumnType(ColType colType);

	/**
	 * @return 当前字段是否是主键（包括复合主键）
	 */
	boolean isPk();

	/**
	 * @return 当前字段是否是复合主键
	 */
	boolean isCompositePk();

	/**
	 * @return 当前字段是否是数字型主键
	 */
	boolean isId();

	/**
	 * @return 当前字段是否是字符型主键
	 */
	boolean isName();

	/**
	 * @return 当前字段有非空约束
	 */
	boolean isNotNull();

	/**
	 * 将字段设置成非空约束
	 */
	void setAsNotNull();

	/**
	 * 这个判断仅仅对于创建语句有作用。
	 * 
	 * @return 当前字段是否是自增的
	 */
	boolean isAutoIncreasement();

	/**
	 * @return 当前字段是否参与保存操作
	 */
	boolean isInsert();

	/**
	 * @return 当前字段是否参与更新操作
	 */
	boolean isUpdate();

    Field getField();

}
