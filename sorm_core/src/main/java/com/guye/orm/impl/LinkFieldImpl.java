package com.guye.orm.impl;

import java.lang.reflect.Field;

import com.guye.orm.Condition;
import com.guye.orm.Entity;
import com.guye.orm.LinkField;
import com.guye.orm.MappingField;
import com.guye.orm.utils.ConditionWraper;

public class LinkFieldImpl extends AbstractEntityField implements LinkField {

    private Class<?> targetType;

    private Entity<?> target;

    private MappingField hostField;

    private MappingField linkedField;
    
    private int linkType;

    public LinkFieldImpl(Entity<?> entity, Field field ) {
        super(entity,field);
    }

    public RecordAdapter<?> getLinkedEntity() {
        return RecordAdapter.sDefault;
    }

    public MappingField getHostField() {
        return hostField;
    }

    public MappingField getLinkedField() {
        return linkedField;
    }

    public void setLinkedAndHostField( MappingField linkedField , MappingField hostField) {
        this.linkedField = linkedField;
        this.hostField = hostField;
    }

    @Override
    public int getLinkType() {
        return linkType;
    }

    @Override
    public Condition createCondition( Object host ) {
        ConditionWraper wraper =  ConditionWraper.createConditionWraper();
        wraper.where().addEq(linkedField.getColumnName(), hostField.getValue(host));
        return wraper;
    }

    public void setLinkType( int l ) {
        linkType = l;
        
    }

    @Override
    public Class<?> getTargetType() {
        return targetType;
    }

    public void setTargetType( Class<?> targetType ) {
        this.targetType = targetType;
    }

    public Entity<?> getTarget() {
        return target;
    }

    public void setTarget( Entity<?> target ) {
        this.target = target;
    }

}
