package com.guye.orm;

import com.guye.orm.impl.EntityHolder;

import android.database.sqlite.SQLiteDatabase;

/**
 * @author nieyu
 *
 */
public class TableUtil {
    private EntityHolder entityHolder;

    TableUtil(EntityHolder entityHolder) {
        this.entityHolder = entityHolder;
    }

    /**
     * 根据给定类，创建一张数据库表
     * @param classOf 这个类必须有@Table注解，否则会抛出异常 
     * @param database 创建数据表的数据库
     * @return
     */
    public int createTable( Class<?> classOf, SQLiteDatabase database ) {
        DaoConfig config = DaoConfig.getConfig();
        StringBuilder builder = new StringBuilder("CREATE TABLE ");
        builder.append(config.getRecordAdapter(classOf).getTableName(classOf));
        builder.append(" (");
        builder.append(config.getRecordAdapter(classOf).getCreateSql(classOf));
        builder.append(");");
        database.execSQL(builder.toString());
        return 1;
    }

    /**
     * 删除一张数据表
     * @param classOf 这个类必须有@Table注解，否则会抛出异常 
     * @param database 创建数据表的数据库
     * @return
     */
    public int dropTable( Class<?> classOf, SQLiteDatabase database ) {
        DaoConfig config = DaoConfig.getConfig();
        StringBuilder builder = new StringBuilder("DROP TALBE ");
        builder.append(config.getRecordAdapter(classOf).getTableName(classOf)).append(';');
        database.execSQL(builder.toString());
        return 1;
    }
    
}
