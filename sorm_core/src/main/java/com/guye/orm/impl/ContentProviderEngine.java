package com.guye.orm.impl;

import java.util.ArrayList;
import java.util.List;

import com.guye.orm.Condition;
import com.guye.orm.DaoException;
import com.guye.orm.Pager;
import com.guye.orm.utils.Constants;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;

class ContentProviderEngine extends DaoEngine {

    private Uri                                 dsUri;
    private Context                             context;

    private ArrayList<ContentProviderOperation> trans;

    public ContentProviderEngine(Uri ds, Context context, boolean isTrans ) {
        super(isTrans);
        this.dsUri = ds;
        this.context = context;
    }

    @Override
    public Object startTransaction() {
        if (trans == null) {
            trans = new ArrayList<ContentProviderOperation>();
        }
        return trans;
    }

    @Override
    public void endTransaction() {
        // do nothing
    }

    @Override
    public void transactionSuccess() {
        try {
            ContentProviderResult[] cpr = context.getContentResolver().applyBatch(
                    dsUri.getAuthority(), trans);
            if(cpr == null || cpr.length != trans.size()){
                throw new DaoException();
            }
            for (int i = 0; i < cpr.length; i++) {
                if (cpr[i] == null || ( cpr[i].count == null && cpr[i].uri == null)) {
                    throw new DaoException();
                }
            }
        } catch (RemoteException | OperationApplicationException e) {
            throw new DaoException();
        } finally {
            trans = null;
        }
    }

    private final Uri buildPurlSqlUri( Uri uri, String sql ) {
        StringBuilder query = new StringBuilder();
        query.append("?");
        query.append(Constants.QUERY_PARAMETER_ISPURE);
        query.append("=");
        query.append("1");
        query.append("&");
        query.append(Constants.QUERY_PARAMETER_PURESQL);
        query.append("=");
        if (!TextUtils.isEmpty(sql)) {
            query.append(Uri.encode(sql));
        }
        String uriStr = uri.toString() + query.toString();
        return Uri.parse(uriStr);
    }

    @Override
    public int bulkInsert( String table, ContentValues[] data ) {
        Uri uri = Uri.withAppendedPath(dsUri, table);
        if (trans == null) {
            return context.getContentResolver().bulkInsert(uri, data);
        } else {
            for (ContentValues contentValues : data) {
                ContentProviderOperation operation = ContentProviderOperation.newInsert(uri)
                        .withValues(contentValues).build();
                trans.add(operation);
            }
            return 1;
        }
    }

    @Override
    public int insert( String table, ContentValues data ) {
        Uri uri = Uri.withAppendedPath(dsUri, table);
        if (trans == null) {
            return context.getContentResolver().insert(uri, data)==null?-1:1;
        } else {
            ContentProviderOperation operation = ContentProviderOperation.newInsert(uri)
                    .withValues(data).build();
            trans.add(operation);
            return 1;
        }
    }

    @Override
    public int update( String table, ContentValues data, String statements,
            String[] strings ) {
        Uri uri = Uri.withAppendedPath(dsUri, table);
        if (trans == null) {
            context.getContentResolver().update(uri, data, statements,
                    strings);
            return 1;
        } else {
            ContentProviderOperation operation = ContentProviderOperation.newUpdate(uri)
                    .withSelection(statements, strings)
                    .withValues(data).build();
            trans.add(operation);
            return 1;
        }
    }

    @Override
    public int delete( String table, String statements, String[] args ) {
        Uri uri = Uri.withAppendedPath(dsUri, table);
        if (trans == null) {
            return context.getContentResolver().delete(uri, statements,
                    args);
        } else {
            trans.add(ContentProviderOperation.newDelete(uri)
                    .withSelection(statements, args)
                    .build());
            return 1;
        }
    }

    @Override
    public Cursor rawQuery(String table, String sql ) {
       Uri uri = Uri.withAppendedPath(dsUri, table);
       return context.getContentResolver().query(buildPurlSqlUri(uri, sql),
                null, null, null, null);
    }

}
