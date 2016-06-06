package com.guye.orm.testapp.testcase;

import com.guye.orm.testapp.GsonAdapter;
import com.guye.orm.testapp.PojoD;

import android.database.sqlite.SQLiteException;
import com.guye.orm.annotation.ColumnAdapterFlag;


public class OrmDeteteErrorCase extends BaseOrmTestCaseProvide {
    
    public void testDeleteError(){
        try {
            checkDao();
            PojoD d = new PojoD();
            d.name = "a";
            dao.delete(d);
        } catch (Exception e) {
            assertEquals(e.getClass(), SQLiteException.class);
        }
        assertNotNull(GsonAdapter.class.getAnnotation(ColumnAdapterFlag.class));
    }

}
