package com.guye.orm.impl;

import java.lang.reflect.Type;

import com.guye.orm.LinkField;

public abstract class TemplateLinkField<T,S> implements LinkField{

    public String name;
    public Class<?> clazz;
    public int type;
    public RecordAdapter<?> ra;
    public Class<?> targetType;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return getTypeClass();
    }

    @Override
    public Class<?> getTypeClass() {
        return clazz;
    }

    @Override
    public int getLinkType() {
        return type;
    }

    @Override
    public RecordAdapter<?> getLinkedEntity() {
        return ra;
    }
    
    @Override
    public Class<?> getTargetType() {
        return targetType;
    }

}
