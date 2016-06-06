package com.guye.orm.impl;

import com.guye.orm.Condition;

public abstract class LinkedAdapter {
    abstract public String getName();

    abstract public String getColumnName();

    abstract public RecordAdapter getRecordAdapter();

    abstract public Object getHostFieldValue();

    abstract public Object getLinkedFieldValue();

    abstract public Class getLinkedType();

    abstract public Condition createCondition();
}
