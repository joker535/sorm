package com.guye.orm;

import com.guye.orm.impl.RecordAdapter;

public interface RecordAdapterFactory {
    RecordAdapter getRecordAdapter( Class clazz ) ;
}
