package com.guye.orm.testapp.testcase;

import java.util.List;

import com.guye.orm.testapp.DatabaseHelper;
import com.guye.orm.testapp.PojoA;
import com.guye.orm.testapp.PojoAType;
import com.guye.orm.testapp.PojoB;
import com.guye.orm.testapp.PojoD;

import com.guye.orm.DaoConfig;

/**
 * Created by nieyu on 16/5/18.
 */
public class OrmUpdatePojoABySqlSuccessCase extends OrmUpdatePojoASuccessCase {

    @Override
    protected void checkDao() {
        if (dao == null) {
            dao = DaoConfig.getConfig().createDao(getContext(), DatabaseHelper.getInstance(getContext()).getWritableDatabase());
        }
    }

}
