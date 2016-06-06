package com.guye.orm.testapp;

import com.guye.orm.Dao;
import com.guye.orm.DaoConfig;
import com.guye.orm.impl.DaoImpl;
import com.guye.orm.testapp.PojoH.PojoHIn1.PojoHInner;
import com.guye.orm.utils.ConditionWraper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "sqlite-test.db";

    private Context             context;

    private Dao                 dao;

    private DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 5);
        this.context = context;
    }

    @Override
    public void onCreate( SQLiteDatabase db ) {
        DaoConfig.getConfig().getTableUtil().createTable(PojoB.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoA.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoC.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoD.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoE.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoF.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoG.class, db);
        DaoConfig.getConfig().getTableUtil().createTable(PojoHInner.class, db);
    }

    @Override
    public void onUpgrade( SQLiteDatabase db, int arg1, int arg2 ) {
    }

    private static DatabaseHelper databaseHelper;

    public static synchronized DatabaseHelper getInstance( Context context ) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context.getApplicationContext());
        }
        return databaseHelper;
    }

    public Dao getDao() {
        if(dao == null){
            dao = DaoConfig.getConfig().createDao(context , getWritableDatabase());
        }
        return dao;
    }

    @Override
    public synchronized void close() {
        super.close();
    }

}
