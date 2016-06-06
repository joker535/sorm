package com.guye.orm.testapp.testcase;

import java.util.List;

import com.guye.orm.testapp.PojoA;
import com.guye.orm.testapp.PojoAType;
import com.guye.orm.testapp.PojoB;
import com.guye.orm.testapp.PojoD;

/**
 * Created by nieyu on 16/5/18.
 */
public class OrmUpdatePojoASuccessCase extends BaseOrmTestCaseProvide {

    public void testUpdateSuccess() {
        checkDao();

        assertNotSame(dao.clear(PojoA.class), -1);
        assertNotSame(dao.clear(PojoB.class), -1);

        PojoA a = new PojoA();
        a.name = "name100";
        a.age = 23;
        a.data = new byte[]{1, 2, 3, 4, 5};
        a.firend = "fname222";
        a.fristName = "frist333";
        a.memory = 3293.323;
        a.type = PojoAType.typeb;
        a.isB = true;
        a.pojod = new PojoD();
        a.pojod.isB = a.isB;
        a.pojod.name = a.name;
        a.pojoB = new PojoB();
        a.pojoB.name = a.name;
        a.pojoB.something = 32;
        a.pojoB.something2 = Integer.MIN_VALUE;
        a.pojoB.something3 = Integer.MAX_VALUE;
        a.pojoB.st = "stsdf";

        assertNotSame(dao.insertWith(a, null), -1);

        a.age = 300;
        a.firend = "dddd";
        assertNotSame(dao.update(a, "age"), -1);

        List<PojoA> list = dao.query(PojoA.class , null);
        assertEquals(list.size() , 1);
        assertEquals(list.get(0).name , "name100");
        assertEquals(list.get(0).firend , "fname222");

        assertNotSame(dao.clear(PojoA.class), -1);
        assertNotSame(dao.clear(PojoB.class), -1);
    }
}
