package com.guye.orm.testapp;

import com.guye.orm.OrmProvider;

import android.database.sqlite.SQLiteDatabase;


public class ProviderTest extends OrmProvider {

    public static final String SINAWEIBO_PROVIDER_AUTHORITY = "com.guye.orm.testapp.blogProvider";

    private DatabaseHelper     databaseHelper;

    private void checkDB() {
        if (databaseHelper == null) {
            databaseHelper = DatabaseHelper.getInstance(getContext());
        }
    }

    @Override
    protected SQLiteDatabase getWritableDatabase() {
        checkDB();
        return databaseHelper.getWritableDatabase();
    }


}
