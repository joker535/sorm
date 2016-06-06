package com.guye.orm.impl;

import java.lang.reflect.Field;

import com.guye.orm.ColumnAdapter;
import com.guye.orm.DaoException;
import com.guye.orm.Entity;
import com.guye.orm.MappingField;
import com.guye.orm.annotation.ColType;

import android.content.ContentValues;
import android.database.Cursor;

public class MappingFieldImpl<T> extends AbstractEntityField implements MappingField {

    private String           columnName;

    private ColType columnType;

    private boolean          isCompositePk;

    private boolean          isId;

    private boolean          isName;

    private boolean          notNull;

    private boolean          autoIncreasement;

    private ColumnAdapter<T> adaptor;

    private boolean          insert = true;

    private boolean          update = true;

    public MappingFieldImpl(Entity<?> entity, Field field) {
        super(entity, field);
    }

    public void injectValue( Object obj, ContentValues rec) {
         Object val = rec.get(columnName );
         this.setValue(obj, val);
    }

    public void injectValue( Object obj, Cursor rs ) {
         Object val = adaptor.get((Class<T>) getTypeClass(),getColumnName(), rs);
         this.setValue(obj, val);
    }

    @Override
    public void enjectValue( Object obj,  ContentValues rec ) {
        if(obj == null){
            return;
        }
        if(isId() && isAutoIncreasement()){
            return;
        }
        try{
            Object object = getValue(obj);
            adaptor.set((Class<T>) getTypeClass(),getColumnName(), (T)object, rec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DaoException(e);
        }

    }

    public String getColumnName() {
        return columnName;
    }

    public ColType getColumnType() {
        return columnType;
    }

    public boolean isCompositePk() {
        return isCompositePk;
    }

    public boolean isPk() {
        return isId || (!isId && isName) || isCompositePk;
    }

    public boolean isId() {
        return isId;
    }

    public boolean isName() {
        return isName;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isAutoIncreasement() {
        return autoIncreasement;
    }

    public void setColumnName( String columnName ) {
        this.columnName = columnName;
    }

    public void setColumnType( ColType columnType ) {
        this.columnType = columnType;
    }

    public void setAsCompositePk() {
        this.isCompositePk = true;
    }

    void setAsId() {
        this.isId = true;
    }

    void setAsName() {
        this.isName = true;
    }

    public void setAsNotNull() {
        this.notNull = true;
    }

    void setAsAutoIncreasement() {
        this.autoIncreasement = true;
    }

    public boolean isInsert() {
        return insert;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setInsert( boolean insert ) {
        this.insert = insert;
    }

    public void setUpdate( boolean update ) {
        this.update = update;
    }

    public ColumnAdapter<T> getAdaptor() {
        return adaptor;
    }

    public void setAdaptor( ColumnAdapter<T> adaptor ) {
        this.adaptor = adaptor;
    }

}
