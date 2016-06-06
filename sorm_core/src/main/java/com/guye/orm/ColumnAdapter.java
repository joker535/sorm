package com.guye.orm;

import android.content.ContentValues;
import android.database.Cursor;

import java.lang.reflect.Field;

import com.guye.orm.annotation.ColumnAdapterFlag;

/**
 * @author nieyu 自己控制某一列数据存储到db中的格式。
 */
@ColumnAdapterFlag
public abstract class ColumnAdapter<T> {

    /**
     * 从结果集中获取对应列所映射的对象字段的值
     * @param fieldType
     * @param cs 数据集
     * @return 对应字段的值
     */
   public abstract T get(Class<T> fieldType, String columnName, Cursor cs );

    /**
     * 将对象中对应字段的值转换为数据库中的值，并设置到ContentValues
     * @param fieldType
     * @param t 对应字段的值
     * @param values
     */
    public abstract void set(Class<T> fieldType, String columnName, T t, ContentValues values );
}
