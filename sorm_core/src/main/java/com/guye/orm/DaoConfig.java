package com.guye.orm;

import java.lang.reflect.InvocationTargetException;

import com.guye.orm.impl.EntityHolder;
import com.guye.orm.impl.RecordAdapter;
import com.guye.orm.impl.DaoImpl;
import com.guye.orm.utils.Constants;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.LruCache;
import android.util.Log;

public class DaoConfig {

    private static DaoConfig config;

    public synchronized static DaoConfig getConfig() {
        if (config == null) {
            config = new DaoConfig();
        }
        return config;
    }

    public static final int                 DEFAULT_CACHESIZE = 16;
    private EntityHolder                    entityHolder;
    private TableUtil                       tableUtil;
    private LruCache<String, ColumnAdapter> adapterCache;
    private RecordAdapterFactory            adapterFactory;
    private boolean                         loadFactory= false;

    private DaoConfig() {
        entityHolder = new EntityHolder();
        tableUtil = new TableUtil(entityHolder);
        adapterCache = new LruCache<>(DEFAULT_CACHESIZE);
    }

    public TableUtil getTableUtil() {
        return tableUtil;
    }

    private void cache( ColumnAdapter adapter ) {
        adapterCache.put(adapter.getClass().getName(), adapter);
    }

    public ColumnAdapter getAdapter( String name ) {
        ColumnAdapter adapter = adapterCache.get(name);
        if(adapter == null){
            try {
                adapter =  (ColumnAdapter) Class.forName(name).newInstance();
            } catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
                throw new IllegalArgumentException(name +"can not create instance");
            }
            cache(adapter);
        }
        return adapter;
    }
    
    public <T> RecordAdapter<T> getRecordAdapter( Class<T> classOf ) {
        if(!loadFactory){
            try{
                adapterFactory = (RecordAdapterFactory)Class.forName(Constants.CORE_PKGNAME+".SOrmAdapterStaticCol").newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                throw new IllegalArgumentException(classOf.getName() +"can not create instance");
            }catch(ClassNotFoundException  e){}
            finally{
                loadFactory = true;
            }
        }
        if(adapterFactory == null){
            return RecordAdapter.sDefault;
        }else{
            RecordAdapter<T> adapter = adapterFactory.getRecordAdapter(classOf);
            if(adapter == null){
                return RecordAdapter.sDefault;
            }else{
                return adapter;
            }
        }
    }

    public EntityHolder getEntityHolder() {
        return entityHolder;
    }

    public Dao createDao( Context context, SQLiteDatabase ds ) {
       return new DaoImpl(context, ds);
    }

    public Dao createDao( Context context, Uri uri ) {
        return new DaoImpl(context, uri);
    }

    public Dao createDaoCompound( Context context, Uri uri ) {
        ContentProviderClient pc = context.getContentResolver().acquireContentProviderClient(uri);
        OrmProvider wOrmProvider = (OrmProvider) pc.getLocalContentProvider();
        pc.release();
        if(wOrmProvider == null){
            return createDao(context,uri);
        }else{
            return createDao(context , wOrmProvider.getWritableDatabase());
        }
    }

}
