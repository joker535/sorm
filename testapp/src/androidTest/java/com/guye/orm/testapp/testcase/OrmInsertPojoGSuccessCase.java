package com.guye.orm.testapp.testcase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.guye.orm.testapp.PojoA;
import com.guye.orm.testapp.PojoAType;
import com.guye.orm.testapp.PojoG;

import com.guye.orm.Dao;
import com.guye.orm.Transaction;

public class OrmInsertPojoGSuccessCase extends BaseOrmTestCaseProvide {
    public void testInsertSuccess() {
        checkDao();

        assertNotSame(dao.clear(PojoG.class) , -1);

        Random random = new Random(System.currentTimeMillis());
        final List<PojoG> as = new ArrayList<PojoG>(50);
        for (int i = 0; i < 50; i++) {
            PojoG e = new PojoG();
            e.ba = (byte) random.nextInt(128);
            e.bc = random.nextBoolean();
            byte[] bs = new byte[random.nextInt(500)];
            random.nextBytes(bs);
            e.bs = bs;
            e.ca = (char) random.nextInt(65530);
            e.da = 1000*random.nextDouble();
            e.fa = 515*random.nextFloat();
            e.ia = random.nextInt();
            e.la = random.nextLong();
            e.sa = "dsafdf"+random.nextInt();
            e.sha = (short) random.nextInt(65530);
           as.add(e);
        }

        int r = dao.exeTransaction(new Transaction() {

            @Override
            public void call( Dao dao ) {
                dao.insertWith(as, null);
            }
        });
        assertEquals(r, 1);
        
        List<PojoG> list = dao.query(PojoG.class, null);
        assertEquals(list.size(), 50);
        assertNotSame(list.get(0).sa,"");
        assertNotSame(list.get(0).sa,null);
        assertNotSame(dao.clear(PojoG.class) , -1);
    }
}
