package com.guye.orm.testapp.testcase;

import com.guye.orm.testapp.DatabaseHelper;

import com.guye.orm.DaoConfig;


public class OrmInsertPojoABySqliteErrorCase extends OrmInsertPojoAErrorCase{
    
    protected void checkDao() {
        if (dao == null) {
            dao = DaoConfig.getConfig().createDao(getContext(), DatabaseHelper.getInstance(getContext()).getWritableDatabase());
        }
    }
}
