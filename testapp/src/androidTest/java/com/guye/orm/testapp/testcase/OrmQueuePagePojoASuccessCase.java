package com.guye.orm.testapp.testcase;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.guye.orm.testapp.PojoA;
import com.guye.orm.testapp.PojoAType;
import com.guye.orm.testapp.PojoB;
import com.guye.orm.testapp.PojoD;

import com.guye.orm.Dao;
import com.guye.orm.Pager;
import com.guye.orm.Transaction;
import com.guye.orm.utils.ConditionWraper;

public class OrmQueuePagePojoASuccessCase extends BaseOrmTestCaseProvide {
    public void testInsertSuccess() {
        checkDao();

        assertNotSame(dao.clear(PojoA.class) , -1);
        assertNotSame(dao.clear(PojoB.class) , -1);

        Random random = new Random(System.currentTimeMillis());
        final List<PojoA> as = new ArrayList<PojoA>(50);
        for (int i = 0; i < 50; i++) {
            PojoA a = new PojoA();
            a.name = "name" + (i);
            a.age = random.nextInt(90);
            a.data = new byte[random.nextInt(30)];
            random.nextBytes(a.data);
            a.firend = "fname" + random.nextInt(2000);
            a.fristName = "frist" + random.nextInt(2000);
            a.memory = random.nextInt(10000) * random.nextDouble();
            a.type = PojoAType.values()[random.nextInt(3)];
            a.isB = random.nextBoolean();
            a.pojod = new PojoD();
            a.pojod.isB = a.isB;
            a.pojod.name = a.name;
            a.pojoB = new PojoB();
            a.pojoB.name = a.name;
            a.pojoB.something = random.nextInt(200);
            a.pojoB.something2 = random.nextInt(200);
            a.pojoB.something3 = random.nextInt(200);
            a.pojoB.st = "st" + random.nextInt(2000);
            as.add(a);
        }
        for (PojoA a : as) {
            Log.d(getName(), a.name);
        }
        int r = dao.exeTransaction(new Transaction() {

            @Override
            public void call( Dao dao ) {
                dao.insertWith(as, null);
            }
        });
        assertEquals(r, 1);

        Pager pager = new Pager();
        pager.setPageNumber(1);
        pager.setPageSize(10);
        List<PojoA> list = dao.query(PojoA.class, null,pager);
        assertEquals(list.size(),10);

        pager.setPageNumber(2);
        Log.d(getClass().getSimpleName() , pager.toSql());
        list = dao.query(PojoA.class, null,pager);
        assertEquals(list.size(),10);

        assertNotSame(dao.clear(PojoA.class) , -1);
        assertNotSame(dao.clear(PojoB.class) , -1);
    }
}
