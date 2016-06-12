package com.guye.orm.impl;

import java.util.Collections;
import java.util.List;

import com.guye.orm.Condition;
import com.guye.orm.Dao;
import com.guye.orm.DaoConfig;
import com.guye.orm.DaoException;
import com.guye.orm.LinkField;
import com.guye.orm.Pager;
import com.guye.orm.PkType;
import com.guye.orm.Transaction;
import com.guye.orm.utils.Each;
import com.guye.orm.utils.Lang;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * 
 * @author nieyu
 * dao实现类
 */
public class DaoImpl implements Dao{

	protected Object ds;
	
	private DaoEngine engine;
	
	protected Context context;
	
	private final boolean isTrans;
	
	private DaoConfig config;
	
	public DaoImpl(Context context , SQLiteDatabase ds) {
	    this(context, ds ,false);
	}
	
	public DaoImpl(Context context , Uri ds) {
	    this(context, ds,false);
	}
	
	private DaoImpl(Context context , Object ds , boolean isTrans) {
	    if(context == null || ds == null){
	        throw new IllegalArgumentException("args cat not be null");
	    }
	    this.context = context.getApplicationContext();
		this.ds = ds;
		if(ds instanceof Uri){
		    engine = new ContentProviderEngine((Uri)ds , this.context , isTrans);
		}else if(ds instanceof SQLiteDatabase){
		    engine = new SqliteEngine((SQLiteDatabase)ds , isTrans);
		}
		this.isTrans = isTrans;
		config = DaoConfig.getConfig();
	}

    @Override
    public <T> int insert( T obj ) {
        int r= engine.insert(Lang.first(obj).getClass(), obj);
        return _checkResult(r);
    }
    
    @Override
    public <T> int insertWith( T obj,final String regex ) {
        int r = insert(obj);
        if(r != -1){
            insertLink(obj, regex);
        }
        return _checkResult(r);
    }

    @Override
    public <T> int insertLink( T obj,final String regex ) {
        final Class clazz = Lang.first(obj).getClass();
        final RecordAdapter<T> entity = (RecordAdapter<T>) config.getRecordAdapter(clazz);
        Lang.each(obj, new Each<T>() {

            @Override
            public int invoke( int index, T ele, int length ) {
                List<LinkField> fields = entity.getOneLinkFields(clazz,regex);
                int r;
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    r = engine.insert(classOfT, linkField.getValue(ele));
                    _checkResult(r);
                }
                fields = entity.getManyLinkFields(clazz,regex);
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    r = engine.insert(classOfT, linkField.getValue(ele));
                    _checkResult(r);
                }
                return Each.CONTINUE;
            }
        });
        return 1;
    }

    @Override
    public <T> int update( T obj ) {
        int r= engine.update(Lang.first(obj).getClass(), obj , null);
        return _checkResult(r);
    }

    @Override
    public <T> int update(T obj, String regex ) {
        int r= engine.update(Lang.first(obj).getClass(), obj, regex);
        return _checkResult(r);
    }
    
    @Override
    public <T> int updateLink( T obj,final String regex ) {
        final Class clazz = Lang.first(obj).getClass();
        final RecordAdapter<T> entity = (RecordAdapter<T>) config.getRecordAdapter(clazz);
        Lang.each(obj, new Each<T>() {
            
            @Override
            public int invoke( int index, T ele, int length ) {
                int r;
                List<LinkField> fields = entity.getOneLinkFields(clazz,regex);
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    r= engine.update(classOfT, linkField.getValue(ele),  regex);
                    _checkResult(r);
                }
                fields = entity.getManyLinkFields(clazz,regex);
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    r =engine.update(classOfT, linkField.getValue(ele), regex);
                    _checkResult(r);
                }
                return Each.CONTINUE;
            }
        });
        return 1;
    }

    @Override
    public <T> int updateWith( T obj,final String regex ) {
        int result = update(obj, regex);
        updateLink(obj, regex);
        return _checkResult(result);
    }

    
    public <T> int update(Class<T> classOfT , ContentValues values , Condition cnd){
        int result = engine.update(classOfT , values , cnd);
        return _checkResult(result);
    }
    
    @Override
    public <T> List<T> query( Class<T> classOfT, Condition cnd, Pager pager ) {
        return engine.query(classOfT, cnd, pager);
    }

    @Override
    public <T> List<T> query( Class<T> classOfT, Condition cnd ) {
        return engine.query(classOfT, cnd, null);
    }
    
    @Override
    public <T> int queryLink( List<T> list,final String regex ) {
        if(Lang.isEmpty(list)){
            return 0;
        }
        final Class clazz = Lang.first(list).getClass();
        final RecordAdapter<T> entity = (RecordAdapter<T>) config.getRecordAdapter(clazz);
        Lang.each(list, new Each<T>() {
            
            @Override
            public int invoke( int index, T ele, int length ) {
                List<LinkField> fields = entity.getOneLinkFields(clazz,regex);
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    Object object = engine.query(classOfT, linkField.createCondition(ele), null);
                    linkField.setValue(ele, Lang.first(object));
                }
                fields = entity.getManyLinkFields(clazz,regex);
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    linkField.setValue(ele, engine.query(classOfT, linkField.createCondition(ele), null));
                }
                return Each.CONTINUE;
            }
        });
        return 1;
    }

    @Override
    public int delete( Class<?> classOfT, long id ) {
        return _checkResult(engine.delete(classOfT, id));
    }

    @Override
    public int delete( Class<?> classOfT, String name ) {
        return _checkResult(engine.delete(classOfT, name));
    }

    @Override
    public <T> int deletex( Class<T> classOfT, Object... pks ) {
        return _checkResult(engine.deletex(classOfT, pks));
    }

    @Override
    public int delete( Object obj ) {
        int r = engine.delete(Lang.first(obj).getClass(), obj);
        return _checkResult(r);
    }

    @Override
    public int deleteLink( Object obj,final String regex ) {
        final Class clazz = Lang.first(obj).getClass();
        final RecordAdapter entity = config.getRecordAdapter(clazz);
        Lang.each(obj, new Each<Object>() {
            
            @Override
            public int invoke( int index, Object ele, int length ) {
                List<LinkField> fields = entity.getOneLinkFields(clazz,regex);
                int r;
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    PkType pkType = linkField.getLinkedEntity().getPkType(classOfT);
                    if(pkType == PkType.ID){
                      r = engine.delete(classOfT, (long)linkField.getValue(ele));
                      _checkResult(r);
                    }else if(pkType == PkType.NAME){
                      r = engine.delete(classOfT, linkField.getValue(ele).toString());
                      _checkResult(r);
                    }else{
                        throw new IllegalArgumentException("cat not link to target :"+linkField.getLinkedEntity().getTableName(classOfT));
                    }
                }
                fields = entity.getManyLinkFields(clazz,regex);
                for (LinkField linkField : fields) {
                    Class classOfT = linkField.getTargetType();
                    r= engine.delete(classOfT, linkField.createCondition(ele));
                    _checkResult(r);
                }
                return Each.CONTINUE;
            }
        });
        return 1;
    }

    @Override
    public int deleteWith( Object obj, String regex ) {
        int r = delete(obj);
        if(r != -1){
            deleteLink(obj, regex);
        }
        return _checkResult(r);
    }
    
    @Override
    public int clear( Class<?> classOfT, Condition cnd ) {
        int r= engine.clean(classOfT, cnd);
        return _checkResult(r);
    }

    @Override
    public int clear( Class<?> classOfT ) {
        int r = engine.clean(classOfT, null);
        return _checkResult(r);
    }

    @Override
    public int count( Class<?> classOfT, Condition cnd ) {
        return engine.count(classOfT, cnd);
    }

    @Override
    public int count(Class<?> classOfT) {
        return engine.count(classOfT, null);
    }

    @Override
    public int exeTransaction(Transaction transaction) {
        int result = -1;
        DaoImpl dao ;
        if(isTrans){
            dao = this;
        }else{
            dao = new DaoImpl(context, ds ,true);
        }
        try {
            dao.engine.startTransaction();
            transaction.call(dao);
            dao.engine.transactionSuccess();
            result = 1;
        } catch(Exception e){
            e.printStackTrace();
        	return result;
        }finally{
            dao.engine.endTransaction();
        }
        return result;
    }

    private int _checkResult( int r ) {
        if(r == -1 && isTrans){
            throw new DaoException();
        }
        return r;
    }
}
