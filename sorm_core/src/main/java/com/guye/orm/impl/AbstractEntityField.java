package com.guye.orm.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.guye.orm.DaoException;
import com.guye.orm.Entity;
import com.guye.orm.EntityField;
import com.guye.orm.utils.Lang;
import com.guye.orm.utils.Mirror;

public abstract class AbstractEntityField implements EntityField {

    private Entity<?> entity;

    private String    name;

    private Field     field;

    private Type      type;

    private Class<?>  typeClass;

    private Mirror<?> mirror;
    
    boolean useGetAndSet;

    public AbstractEntityField(Entity<?> entity, Field field) {
        this.entity = entity;
        this.field = field;
        this.name = field.getName();
    }

    public Entity<?> getEntity() {
        return entity;
    }

    public void setValue( Object obj, Object value ) {
        if (value == null) {
            return;
        }
        if (useGetAndSet()) {
            try {
                Method setter = null;
                try{
                    setter = getEntity().getMirror().getSetter(getField());
                }catch (NoSuchMethodException e){}

                if(setter != null){
                    Class<?> valueType = setter.getParameterTypes()[0];
                    if(value.getClass()!= valueType){
                        Mirror<?> m = Mirror.me(value.getClass());
                        if (!mirror.isWrapperOf(value.getClass()) && !m.isWrapperOf(valueType)) {
                            value = Lang.castTo(value, getField().getType());
                        }
                    }
                    setter.invoke(obj, value);
                    return;
                }
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new DaoException(String.format("Fail to set '%s'[ %s ] to field %s.['%s']",
                        value, value==null?null:value.getClass().getName(),
                        getField().getDeclaringClass().getName(), getField().getName()),
                        Lang.unwrapThrow(e), Lang.unwrapThrow(e).getMessage());
            }

        }
        try {
            
            if(value.getClass()!=typeClass){
                Mirror<?> m = Mirror.me(value.getClass());
                if (!mirror.isWrapperOf(value.getClass()) && !m.isWrapperOf(typeClass)) {
                    value = Lang.castTo(value, getField().getType());
                }
            }
            getField().set(obj, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new DaoException(String.format("Fail to set '%s'[ %s ] to field %s.['%s']",
                    value, value==null?null:value.getClass().getName(),
                    getField().getDeclaringClass().getName(), getField().getName()),
                    Lang.unwrapThrow(e), Lang.unwrapThrow(e).getMessage());
        }


    }

    public Object getValue( Object obj ) {
        if(useGetAndSet()){
            Method getter = null;
            try {
                try{
                    getter = getEntity().getMirror().getGetter(getField());
                }catch (NoSuchMethodException e){}
                if(getter != null){
                    return null == obj ? null : getter.invoke(obj);
                }
            } catch ( IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new DaoException(String.format("Fail to get field %s.'%s' because [%s]: %s",
                        getField().getDeclaringClass().getName(), getField().getName(),
                        Lang.unwrapThrow(e), Lang.unwrapThrow(e).getMessage()));
            }
        }
        try {
            return null == obj ? null : getField().get(obj);
        } catch (Exception e) {
            throw new DaoException(String.format("Fail to get field %s.'%s' because [%s]: %s",
                    getField().getDeclaringClass().getName(), getField().getName(),
                    Lang.unwrapThrow(e), Lang.unwrapThrow(e).getMessage()));
        }

    }

    public boolean useGetAndSet(){
        return useGetAndSet;
    }
    
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public Mirror<?> getTypeMirror() {
        return mirror;
    }

    public void setType( Type type ) {
        this.type = type;
        this.typeClass = Lang.getTypeClass(type);
        this.mirror = Mirror.me(typeClass);
    }

    public String toString() {
        return String.format("'%s'(%s)", this.name, this.entity.getType().getName());
    }

    public Field getField() {
        return field;
    }
}
