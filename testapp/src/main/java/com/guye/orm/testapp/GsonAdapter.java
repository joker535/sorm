package com.guye.orm.testapp;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;
import com.guye.orm.ColumnAdapter;
import com.guye.orm.MappingField;
import com.guye.orm.annotation.ColType;

import java.lang.reflect.Field;

public class GsonAdapter extends ColumnAdapter{

    private Gson gson = new Gson();

    @Override
    public Object get(Class toType, String columnName, Cursor c ) {
        int index = c.getColumnIndex(columnName);
        if(c.isNull(index)){
            return null;
        }
        String json = c.getString(index);
        Object t = gson.fromJson(json, toType);
        return t;
    }

    @Override
    public void set( Class toType, String columnName, Object t, ContentValues values ) {
        String json = gson.toJson(t);
        values.put(columnName  , json);
    }

}
