package com.guye.orm.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.Date;

import com.guye.orm.ColumnAdapter;
import com.guye.orm.DaoException;
import com.guye.orm.MappingField;
import com.guye.orm.annotation.ColType;
import com.guye.orm.utils.Mirror;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class ColumnAdapterDefault {
    
public static final ColumnAdapter<Enum<?>> enumAdapter = new ColumnAdapter<Enum<?>>() {

        @Override
        public Enum<?> get(Class<Enum<?>> toType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            int v = c.getInt(index);
            Enum o = null;

            // 首先试图用采用该类型的 fromInt 的静态方法
            try {
                Method m = toType.getMethod("fromInt", int.class);
                if (Modifier.isStatic(m.getModifiers())
                    && toType.isAssignableFrom(m.getReturnType())) {
                    o = (Enum) m.invoke(null, v);
                }
            }
            catch (Exception e) {}

            // 搞不定，则试图根据顺序号获取
            if (null == o)
                try {
                    for (Field f : toType.getFields()) {
                        if (f.getType() == toType) {
                            Enum em = (Enum) f.get(null);
                            if (em.ordinal() == v)
                                return em;
                        }
                    }
                    throw new DaoException(String.format(
                                         "Can NO find enum value in [%s] by int value '%d'",
                                         toType.getName(),
                                         v));
                }
                catch (Exception e2) {
                    throw new DaoException(String.format(
                            "Can NO find enum value in [%s] by int value '%d'",
                            toType.getName(),
                            v));
                }

            return o;
        }

        @Override
        public void set( Class<Enum<?>> fieldType, String columnName, Enum<?> t, ContentValues values ) {
            values.put(columnName, t.ordinal());
        }
    };  
    public static final ColumnAdapter<Object> intAdapter = new ColumnAdapter<Object>() {
        
        @Override
        public Object get(Class<Object> fieldType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            Mirror mirror = Mirror.me(fieldType);
            if(mirror.isInt()){
                return c.getInt(index);
            }else if(mirror.isShort()){
                return c.getShort(index);
            }else if(mirror.isChar()){
                return (char)c.getShort(index);
            }else if(mirror.isByte()){
                return (byte)c.getShort(index);
            }else if(mirror.isLong()){
                return c.getLong(index);
            }else{
                throw new RuntimeException(fieldType.toString() + " type can not match");
            }
        }

        @Override
        public void set( Class<Object> fieldType, String columnName, Object t, ContentValues values ) {
            Class c = t.getClass();
            Mirror mirror = Mirror.me(c);
            if(mirror.isInt()){
                values.put(columnName, (int)t);
            }else if(mirror.isLong()){
                values.put(columnName, (long)t);
            }else if(mirror.isByte()){
                values.put(columnName, (byte)t);
            }else if(mirror.isShort()){
                values.put(columnName, (short)t);
            }else if(mirror.isChar()){
                values.put(columnName, (short)(char)t);
            }else {
                throw new RuntimeException(fieldType.toString() + "type can not match");
            }
        }
    };  
    
    public static final ColumnAdapter<Object> realAdapter = new ColumnAdapter<Object>() {
        
        @Override
        public Object get(Class<Object> fieldType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            Mirror mirror = Mirror.me(fieldType);
            if(mirror.isFloat()){
                float v = c.getFloat(index);
                return v;
            }else{
                double v = c.getDouble(index);
                return v;
            }
        }

        @Override
        public void set( Class<Object> fieldType, String columnName, Object t, ContentValues values ) {
            Class c = t.getClass();
            Mirror mirror = Mirror.me(c);
            if(mirror.isFloat()){
                values.put(columnName, (float)t);
            }else if(mirror.isDouble()){
                values.put(columnName, (double)t);
            }else{
                throw new RuntimeException(fieldType.toString() + "type can not match");
            }
            
        }
        
    };  
    
    public static final ColumnAdapter dateAdapter = new ColumnAdapter() {
        
        @Override
        public Object get(Class fieldType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            long v = c.getLong(index);

            Mirror mirror = Mirror.me(fieldType);
            if(mirror.is(Date.class)){
                Date date = new Date(v);
                return date;
            }else if(mirror.is(Calendar.class)){
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(v);
                return calendar;
            }else{
                throw new RuntimeException(fieldType.toString() + "type can not match");
            }
        }

        @Override
        public void set( Class fieldType, String columnName, Object t, ContentValues values ) {
            if(t!= null){
                Mirror mirror = Mirror.me(fieldType);
                long d ;
                if(mirror.is(Date.class)){
                    Date date = (Date) t;
                    d = date.getTime();
                }else if(mirror.is(Calendar.class)){
                    Calendar calendar = (Calendar) t;
                    d = calendar.getTimeInMillis();
                }else{
                    throw new RuntimeException(t.toString() + "can not match type :"+fieldType.toString());
                }
                values.put(columnName, d);
            }
            
        }
        
    };  
   
    public static final ColumnAdapter<CharSequence> textAdapter = new ColumnAdapter<CharSequence>() {
        
        @Override
        public String get(Class<CharSequence> fieldType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            String v = c.getString(index);
            return v;
        }

        @Override
        public void set(Class<CharSequence> fieldType, String columnName, CharSequence t, ContentValues values ) {
            values.put(columnName, t.toString());
        }
        
    };  
    public static final ColumnAdapter<Boolean> booleanAdapter = new ColumnAdapter<Boolean>() {

        @Override
        public Boolean get(Class<Boolean> fieldType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            String v = c.getString(index);
            return Boolean.valueOf(v);
        }

        @Override
        public void set( Class<Boolean> fieldType, String columnName, Boolean t, ContentValues values ) {
            values.put(columnName, t.toString());
        }
        
    };  
    
    public static final ColumnAdapter<byte[]> blobAdapter = new ColumnAdapter<byte[]>() {
        
        @Override
        public byte[] get(Class<byte[]> fieldType, String columnName,Cursor c ) {
            int index = c.getColumnIndex(columnName);
            if(c.isNull(index)){
                return null;
            }
            byte[] v = c.getBlob(index);
            return v;
        }

        @Override
        public void set( Class<byte[]> fieldType, String columnName, byte[] t, ContentValues values ) {
            Class c = t.getClass();
            Mirror mirror = Mirror.me(c);
            values.put(columnName, ((byte[])t));
        }
        
    }; 
}
