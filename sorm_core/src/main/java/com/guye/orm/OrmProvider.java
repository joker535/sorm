package com.guye.orm;

import java.util.ArrayList;

import com.guye.orm.utils.Constants;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public abstract class OrmProvider extends ContentProvider {
    private String _getTable( Uri uri ) {
        return uri.getPath().substring(1);
    }

    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs ) {
        return getWritableDatabase().delete(_getTable(uri), selection, selectionArgs);
    }

    protected abstract SQLiteDatabase getWritableDatabase();

    @Override
    public String getType( Uri uri ) {
        return null;
    }

    @Override
    public Uri insert( Uri uri, ContentValues values ) {
        try {
            long r = getWritableDatabase().insert(_getTable(uri), null, values);
            if (r == -1) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return uri;
    }

    @Override
    public int bulkInsert( Uri uri, ContentValues[] values ) {
        int count = values.length;
        for (int i = 0; i < values.length; i++) {
            if(insert(uri, values[i]) == null){
                count--;
            }
        }
        return count;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder ) {

        boolean isPurlSql = isPureSql(uri);
        Cursor cursor = null;
        if (isPurlSql) {
            String sql = getPureSql(uri);
            cursor = getWritableDatabase().rawQuery(sql, null);
        } else {
            cursor = getWritableDatabase().query(_getTable(uri), projection, selection,
                    selectionArgs, null, null, sortOrder);
        }
        return cursor;
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs ) {
        return getWritableDatabase().update(_getTable(uri), values, selection, selectionArgs);
    }

    @Override
    public ContentProviderResult[] applyBatch( ArrayList<ContentProviderOperation> operations )
            throws OperationApplicationException {

        ContentProviderResult[] contentProviderResults;
        try {
            getWritableDatabase().beginTransaction();
            contentProviderResults = new ContentProviderResult[operations
                    .size()];
            
            int i = 0;
            for (ContentProviderOperation cpo : operations) {
                contentProviderResults[i] = cpo.apply(this, contentProviderResults, i);
                if(contentProviderResults[i] == null || (contentProviderResults[i].count == null && contentProviderResults[i].uri == null)){
                    throw new DaoException();
                }
                i++;
            }
            getWritableDatabase().setTransactionSuccessful();
        } finally{
            if (getWritableDatabase().inTransaction()) {
                getWritableDatabase().endTransaction();
            }            
        }
        return contentProviderResults;
    }

    private String getPureSql( Uri uri ) {
        String pureSql = uri.getQueryParameter(Constants.QUERY_PARAMETER_PURESQL);
        if (pureSql == null) {
            return null;
        }
        return Uri.decode(pureSql);
    }

    private boolean isPureSql( Uri uri ) {
        String isPureStr = uri.getQueryParameter(Constants.QUERY_PARAMETER_ISPURE);
        if (TextUtils.isEmpty(isPureStr)) {
            return false;
        }

        int isPureInt = 0;
        try {
            isPureInt = Integer.parseInt(isPureStr);
        } catch (NumberFormatException e) {

        }
        return isPureInt == 1;
    }

}
