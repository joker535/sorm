package com.guye.orm.testapp;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.guye.orm.ColumnAdapter;
import com.guye.orm.MappingField;

public class SerAdapter extends ColumnAdapter<Object>{


    @Override
    public Object get( Class<Object> toType, String columnName, Cursor cs ) {
        int index = cs.getColumnIndex(columnName);
        if(cs.isNull(index)){
            return null;
        }
        try {
            byte[] data = cs.getBlob(index);
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(data));
            Object object =  inputStream.readObject();
            inputStream.close();
            return object;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void set(Class<Object> toType, String columnName, Object t, ContentValues values ) {
        try {
            ByteArrayOutputStream byteOutputStream=new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(t);
            values.put(columnName,byteOutputStream.toByteArray());
            objectOutputStream.close();
            byteOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
