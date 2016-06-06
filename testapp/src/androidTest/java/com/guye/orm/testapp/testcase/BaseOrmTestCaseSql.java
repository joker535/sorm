package com.guye.orm.testapp.testcase;

import com.guye.orm.testapp.DatabaseHelper;
import com.guye.orm.testapp.ProviderTest;

import android.net.Uri;
import android.test.AndroidTestCase;
import com.guye.orm.Dao;
import com.guye.orm.DaoConfig;

public class BaseOrmTestCaseSql extends AndroidTestCase {
    protected Dao dao;

    protected void checkDao() {
        if (dao == null) {
            dao = DaoConfig.getConfig().createDaoCompound(getContext(), Uri.parse("content://"
                    + ProviderTest.SINAWEIBO_PROVIDER_AUTHORITY));
        }
    }

}
