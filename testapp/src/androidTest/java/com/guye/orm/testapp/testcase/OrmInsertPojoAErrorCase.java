package com.guye.orm.testapp.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.guye.orm.testapp.PojoA;
import com.guye.orm.testapp.PojoAType;
import com.guye.orm.testapp.PojoB;

import com.guye.orm.Dao;
import com.guye.orm.Transaction;


public class OrmInsertPojoAErrorCase extends BaseOrmTestCaseProvide {
    public void testInsertError() {
        checkDao();
        Random random = new Random(System.currentTimeMillis());
        final List<PojoA> as = new ArrayList<PojoA>(50);
        for (int i = 0; i < 50; i++) {
            PojoA a = new PojoA();
            a.name = "name" + random.nextInt(3);
            a.age = random.nextInt(90);
            a.data = new byte[random.nextInt(30)];
            random.nextBytes(a.data);
            a.firend = "fname" + random.nextInt(2000);
            a.fristName = "frist" + random.nextInt(2000);
            a.memory = random.nextInt(10000) * random.nextDouble();
            a.type = PojoAType.values()[random.nextInt(3)];
            a.pojoB = new PojoB();
            a.pojoB.name = a.name;
            a.pojoB.something = random.nextInt(200);
            a.pojoB.something2 = random.nextInt(200);
            a.pojoB.something3 = random.nextInt(200);
            a.pojoB.st = "st" + random.nextInt(2000);
            as.add(a);
        }
        int r = dao.exeTransaction(new Transaction() {

            @Override
            public void call( Dao dao ) {
                dao.insertWith(as, null);
            }
        });
        assertEquals(r, -1);
    }
}
