package com.guye.orm.testapp.testcase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.guye.orm.testapp.PojoA;
import com.guye.orm.testapp.PojoAType;
import com.guye.orm.testapp.PojoE;

import com.guye.orm.Dao;
import com.guye.orm.Transaction;

public class OrmInsertPojoESuccessCase extends BaseOrmTestCaseProvide {
    public void testInsertSuccess() {
        checkDao();

        assertNotSame(dao.clear(PojoE.class) , -1);

        Random random = new Random(System.currentTimeMillis());
        final List<PojoE> as = new ArrayList<PojoE>(50);
        for (int i = 0; i < 50; i++) {
            PojoE e = new PojoE();
            e.ba = (byte) random.nextInt(128);
            e.bc = random.nextBoolean();
            byte[] bs = new byte[random.nextInt(500)];
            random.nextBytes(bs);
            e.bs = bs;
            e.ca = (char) random.nextInt(65530);
            e.calendar = Calendar.getInstance();
            e.da = 1000*random.nextDouble();
            e.date = new Date(System.currentTimeMillis());
            e.ea = PojoAType.values()[random.nextInt(3)];
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
        
        List<PojoE> list = dao.query(PojoE.class, null);
        assertEquals(list.size(), 50);

        for (PojoE pojoE : list) {
            assertNotSame(pojoE.getCalendar(),null);
        }
        
        assertNotSame(dao.clear(PojoE.class) , -1);
    }
}
