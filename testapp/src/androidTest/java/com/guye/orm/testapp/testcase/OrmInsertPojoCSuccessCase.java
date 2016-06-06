package com.guye.orm.testapp.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.guye.orm.testapp.PojoC;
import com.guye.orm.testapp.PojoD;

import com.guye.orm.Dao;
import com.guye.orm.Transaction;

public class OrmInsertPojoCSuccessCase extends BaseOrmTestCaseProvide {
    public void testInsertSuccess() {
        checkDao();

        assertNotSame(dao.clear(PojoC.class) , -1);
        assertNotSame(dao.clear(PojoD.class) , -1);

        Random random = new Random(System.currentTimeMillis());
        final List<PojoC> as = new ArrayList<PojoC>(50);
        for (int i = 0; i < 50; i++) {
            PojoC a = new PojoC();
            a.fristName = "fffname"+random.nextInt(900);
            a.id = i;
            a.list = new ArrayList<PojoD>();
            int s = 2+random.nextInt(30);
            for (int j = 0; j < s; j++) {
                PojoD pd = new PojoD();
                pd.name = "name"+i+":"+j;
                pd.isB = random.nextBoolean();
                pd.age = a.id;
                a.list.add(pd);
            }
         
            as.add(a);
        }

        int r = dao.exeTransaction(new Transaction() {

            @Override
            public void call( Dao dao ) {
                dao.insertWith(as, null);
            }
        });
        assertEquals(r, 1);
           
        List<PojoC> list = dao.query(PojoC.class, null);
        dao.queryLink(list , null);
        assertEquals(list.size(), 50);
        for (PojoC pojoC : list) {
            assertNotSame(pojoC.list.size(), 0);
        }

        assertNotSame(dao.clear(PojoC.class) , -1);
        assertNotSame(dao.clear(PojoD.class) , -1);
    }
}
