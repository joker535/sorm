package com.guye.orm.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqliteEngine extends DaoEngine {

    private SQLiteDatabase database;

    public SqliteEngine(SQLiteDatabase sd, boolean isTrans ) {
        super(isTrans);
        database = sd;
    }

    @Override
    public Object startTransaction() {
        database.beginTransaction();
        return 1;
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

    @Override
    public void transactionSuccess() {
        database.setTransactionSuccessful();
    }

    @Override
    public int bulkInsert( String table, ContentValues[] contentValues ) {
        for (ContentValues c : contentValues) {
            database.insert(table, null, c);
        }
        return contentValues.length;
    }

    @Override
    public int insert( String table, ContentValues c ) {
        return (int) database.insert(table, null, c);
    }

    @Override
    public int update( String table, ContentValues contentValues, String statements,
            String[] strings ) {
        return database.update(table, contentValues, statements, strings);
    }

    @Override
    public int delete( String table, String statements, String[] args ) {
        return database
                .delete(table, statements,args);
    }

    @Override
    public Cursor rawQuery( String table, String sql ) {
        return database.rawQuery(sql, null);
    }
}
