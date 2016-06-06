package com.guye.orm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.guye.orm.Condition;
import com.guye.orm.DaoConfig;
import com.guye.orm.Pager;
import com.guye.orm.PkType;
import com.guye.orm.utils.ConditionWraper;
import com.guye.orm.utils.Mirror;
import com.guye.orm.utils.ConditionWraper.SqlLogic;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class DaoEngine {
    private DaoConfig config = DaoConfig.getConfig();
    
    protected boolean isTrans;

    DaoEngine(boolean isTrans){
        this.isTrans = isTrans;
    }
    
    protected <T> RecordAdapter<T> getRecordAdapter( Class<T> classOf ) {
        return config.getRecordAdapter(classOf);
    }

    public <T> int insert( Class<T> classOf, Object obj ) {
        if (obj == null) {
            return 0;
        }
        RecordAdapter entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        ContentValues[] data = generteValues(classOf, obj, null);

        if (data.length == 1) {
            return (int) insert(table, data[0]);
        } else {
            bulkInsert(table, data);
            return data.length;
        }
    }
    
    public <T> int update( Class<T> classOf, ContentValues contentValues, Condition cnd ) {
        if (contentValues == null) {
            return 0;
        }
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        return update(table, contentValues, cnd == null ? null : cnd.where().toStatements(),
                cnd == null ? null : cnd.where().getArgs());

    }

    public <T> int update( Class<T> classOf, Object obj, String regex ) {
        if (obj == null) {
            return 0;
        }
        if (Mirror.me(obj.getClass()).isColl()) {
            throw new IllegalArgumentException("not support Collection or Array args");
        }
        if (classOf != obj.getClass()) {
            throw new IllegalArgumentException("data Type not match");
        }
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        ConditionWraper conditionWraper = ConditionWraper.createConditionWraper();
        ContentValues[] values = generteValues(classOf, obj, regex);

        Map<String, Object> map = entity.getPkNameAndValues((T) obj, classOf);
        if (map == null || map.size() == 0) {
            throw new IllegalArgumentException(classOf.getName() + " object not have PRIMARY KEY");
        }
        Set<String> keys = map.keySet();
        int index = 0, size = map.size();
        for (String string : keys) {
            conditionWraper.where().addEq(string, map.get(string));
            if (index != size - 1) {
                conditionWraper.where().addLogic(SqlLogic.and);
            }
            index++;
        }
        return update(table, values[0], conditionWraper.where().toStatements(), conditionWraper
                .where().getArgs());
    }

    public <T> int delete( Class<T> classOf, Object obj ) {
        if (obj == null) {
            return 0;
        }
        RecordAdapter entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        if (Mirror.me(obj.getClass()).isColl()) {
            throw new IllegalArgumentException("not support Collection or Array args");
        }
        if (classOf != obj.getClass()) {
            throw new IllegalArgumentException("data Type not match");
        }

        ConditionWraper buidler = ConditionWraper.createConditionWraper();

        Map<String, Object> map = entity.getPkNameAndValues((T) obj, classOf);
        if (map == null || map.size() == 0) {
            throw new IllegalArgumentException(classOf.getName() + " object not have PRIMARY KEY");
        }
        Set<String> keys = map.keySet();
        int index = 0, size = map.size();
        for (String string : keys) {
            buidler.where().addEq(string, map.get(string));
            if (index != size - 1) {
                buidler.where().addLogic(SqlLogic.and);
            }
            index++;
        }
        return delete(table, buidler.where().toStatements(), buidler.where().getArgs());
    }

    public <T> int delete( Class<T> classOf, long id ) {
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        if (entity.getPkType(classOf) == PkType.ID) {
            String table = entity.getTableName(classOf);
            ConditionWraper buidler = ConditionWraper.createConditionWraper();
            buidler.where().addEq((String) (entity.getPkNames(classOf)), id);
            return delete(table, buidler.where().toStatements(), buidler.where().getArgs());
        }
        throw new IllegalArgumentException(classOf.getName() + " object not have int PRIMARY KEY");
    }

    public <T> int delete( Class<T> classOf, String name ) {
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        if (entity.getPkType(classOf) == PkType.NAME) {
            String table = entity.getTableName(classOf);
            ConditionWraper buidler = ConditionWraper.createConditionWraper();
            buidler.where().addEq((String) (entity.getPkNames(classOf)), name);
            return delete(table, buidler.where().toStatements(), buidler.where().getArgs());
        }
        throw new IllegalArgumentException(classOf.getName()
                + " object not have string PRIMARY KEY");
    }

    public <T> int deletex( Class<T> classOf, Object... pks ) {
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        if (entity.getPkType(classOf) == PkType.COMPOSITE) {
            String table = entity.getTableName(classOf);
            ConditionWraper buidler = ConditionWraper.createConditionWraper();
            String[] fields = (String[]) entity.getPkNames(classOf);
            Object key;
            if (fields.length != pks.length) {
                throw new IllegalArgumentException(" can not match composite PRIMARY KEY");
            }
            for (int i = 0; i < pks.length; i++) {
                key = pks[i];
                buidler.where().addEq(fields[i], key);
                if (i != pks.length - 1) {
                    buidler.where().addLogic(SqlLogic.and);
                }
            }
            return delete(table, buidler.where().toStatements(), buidler.where().getArgs());
        }
        throw new IllegalArgumentException(classOf.getName()
                + " object not have composite PRIMARY KEY");
    }

    public <T> List<T> query( Class<T> classOf, Condition cnd, Pager pager ) {
        if (isTrans) {
            throw new IllegalStateException("in Transaction not support query option");
        }
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        Cursor cursor = null;
        List<T> list = null;

        try {
            StringBuilder builder = new StringBuilder("SELECT * FROM ").append(table).append(' ')
                    .append(cnd == null ? " " : cnd.toSql())
                    .append(pager == null ? "" : pager.toSql());
            
            cursor = rawQuery(table , builder.toString());
            list = new ArrayList<T>(cursor.getCount());
            cursor.moveToFirst();
            T t;
            while (!cursor.isAfterLast()) {
                t = entity.bronObject(classOf);
                entity.getObject(cursor, classOf , t);
                list.add(t);
                cursor.moveToNext();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    public <T> int clean( Class<T> classOf, Condition cnd ) {
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        return delete(table, cnd == null ? null : cnd.where().toStatements(), cnd == null ? null
                : cnd.where().getArgs());
    }

    public <T> int count( Class<T> classOf, Condition cnd ) {
        if (isTrans) {
            throw new IllegalStateException("in Transaction not support count option");
        }
        RecordAdapter<T> entity = getRecordAdapter(classOf);
        String table = entity.getTableName(classOf);
        int result = 0;
        Cursor c = null;
        StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ").append(table).append(
                cnd == null ? " " : cnd.toSql());
        try {
            c = rawQuery(table ,builder.toString());
            if (c == null) {
                return 0;
            }
            if (c.moveToFirst()) {
                result = (int) c.getLong(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
      
        return result;
    }
    
    protected <T> ContentValues[] generteValues( Class<T> classOf, Object obj, String regex ) {
        ContentValues[] data;
        RecordAdapter entity = config.getRecordAdapter(classOf);
        if (obj.getClass().isArray()) {
            T[] collection = (T[]) obj;
            data = new ContentValues[collection.length];
            int i = 0;
            for (Object object : collection) {
                data[i++] = entity.createRecord((T) object, regex);
            }
        } else if (obj instanceof Collection) {
            Collection<T> collection = (Collection) obj;
            Iterator<T> iterator = collection.iterator();
            data = new ContentValues[collection.size()];
            int i = 0;
            for (T object : collection) {
                if (classOf != object.getClass()) {
                    throw new IllegalArgumentException("data Type not match");
                }
                data[i++] = entity.createRecord(object, regex);
            }
        } else if (classOf == obj.getClass()) {
            data = new ContentValues[1];
            data[0] = entity.createRecord((T) obj, regex);
        } else {
            throw new IllegalArgumentException("data Type not match");
        }
        return data;
    }

    abstract public int bulkInsert( String table, ContentValues[] contentValues );
    abstract public int insert( String table, ContentValues contentValues );

    abstract public int update( String table, ContentValues contentValues, String statements, String[] strings );

    abstract public int delete( String table, String statements, String[] args ) ;

    abstract public Cursor rawQuery( String table, String sql  );
    public abstract Object startTransaction();
    public abstract void endTransaction();
    public abstract void transactionSuccess();
}
