package com.guye.orm.testapp.testcase;

import junit.framework.TestSuite;
import android.test.InstrumentationTestRunner;

public class OrmTestRunner extends InstrumentationTestRunner{
    
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new TestSuite("All tests from part1");//表明这个标识性东西
        suite.addTestSuite(OrmDeteteErrorCase.class);
        suite.addTestSuite(OrmInsertPojoAErrorCase.class);
        suite.addTestSuite(OrmInsertPojoABySqliteErrorCase.class);
        suite.addTestSuite(OrmInsertPojoASuccessCase.class);
        suite.addTestSuite(OrmUpdatePojoASuccessCase.class);
        suite.addTestSuite(OrmUpdatePojoABySqlSuccessCase.class);
        suite.addTestSuite(OrmQueuePagePojoASuccessCase.class);
        suite.addTestSuite(OrmInsertPojoCSuccessCase.class);
        suite.addTestSuite(OrmInsertPojoESuccessCase.class);
        suite.addTestSuite(OrmInsertPojoFSuccessCase.class);
        suite.addTestSuite(OrmInsertPojoGSuccessCase.class);
        suite.addTestSuite(OrmInsertPojoHInnerSuccessCase.class);
        return suite;
    }
}
