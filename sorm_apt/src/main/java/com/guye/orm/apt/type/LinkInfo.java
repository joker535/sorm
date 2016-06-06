package com.guye.orm.apt.type;

import javax.annotation.processing.Messager;

/**
 * Created by nieyu on 16/5/17.
 */
public class LinkInfo extends ColumnInfo{
    
    public static final int ONE = 1;
    public static final int MANY = 2;

    private int linkType;

    private EntityInfo targetType;

    private String mapKey;

    private ColumnInfo hostField;

    private ColumnInfo linkedField;

    public LinkInfo(Messager msg) {
        super(msg);
    }

    
    public static int getONE() {
        return ONE;
    }

    public static int getMANY() {
        return MANY;
    }

    public int getLinkType() {
        return linkType;
    }

    public EntityInfo getTargetType() {
        return targetType;
    }

    public String getMapKey() {
        return mapKey;
    }

    public ColumnInfo getHostField() {
        return hostField;
    }

    public ColumnInfo getLinkedField() {
        return linkedField;
    }

    public void setLinkType(int linkType) {
        this.linkType = linkType;
    }

    public void setTargetType(EntityInfo targetType) {
        this.targetType = targetType;
    }

    public void setMapKey(String mapKey) {
        this.mapKey = mapKey;
    }

    public void setHostField(ColumnInfo hostField) {
        this.hostField = hostField;
    }

    public void setLinkedField(ColumnInfo linkedField) {
        this.linkedField = linkedField;
    }
}
