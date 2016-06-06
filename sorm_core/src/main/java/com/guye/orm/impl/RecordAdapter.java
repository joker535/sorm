package com.guye.orm.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.guye.orm.DaoConfig;
import com.guye.orm.Entity;
import com.guye.orm.EntityField;
import com.guye.orm.LinkField;
import com.guye.orm.MappingField;
import com.guye.orm.PkType;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by nieyu on 16/5/16.
 * javabean 和数据库 record的适配器。负责二者之间转换。
 */
public abstract class RecordAdapter<T> {
    
    /**
     * 新建一个实体对象
     * @param classOfT
     * @return
     */
    abstract public T bronObject(Class classOfT);
    /**
     * 用cs的当前列填充实体对象t
     * @param cs
     * @param classOfT
     * @param t
     * @return
     */
    abstract public T getObject(Cursor cs , Class classOfT,T t);
    /**
     * 用实体对象t转换为一个ContentValues
     * @param t
     * @param regex
     * @return
     */
    abstract public ContentValues createRecord(T t,String regex);
    /**
     * 返回创建table的sql语句的value部分，create table xxx (?),即问号对应的部分
     * @param classOf
     * @return
     */
    abstract public String getCreateSql(Class classOf);
    /**
     * 获取实体对应表名
     * @param classOf
     * @return
     */
    abstract public String getTableName(Class classOf);
    /**
     * 获取实体对应表的主键类型，如果没有主键返回UNKONW，不会返回null
     * @param classOfT
     * @return
     */
    abstract public PkType getPkType(Class classOfT);
    /**
     * 获取实体对应表的主键名，没有返回null，单一主键返回String，复合主键返回String[]
     * @param classOfT
     * @return
     */
    abstract public Object getPkNames(Class classOfT);
    /**
     * 获取实体对象对应表的主键名和值，没有返回空map。
     * @param t
     * @param classOfT
     * @return
     */
    abstract public Map<String, Object> getPkNameAndValues(T t,Class classOfT);
    /**
     *获取实体类所有匹配上正则表达式的一对一关联字段，如果正则表达是为 null，则表示获取全部关联字段
     * 
     * @param regex
     *            正则表达式
     * 
     * @return 实体所有匹配上正则表达是的关联字段，没有关联返回空list
     */
    abstract public List<LinkField> getOneLinkFields(Class classOfT, String regex );
    /**
     *获取实体类所有匹配上正则表达式的一对多关联字段，如果正则表达是为 null，则表示获取全部关联字段
     * 
     * @param regex
     *            正则表达式
     * 
     * @return 实体所有匹配上正则表达是的关联字段，没有关联返回空list
     */
    abstract public List<LinkField> getManyLinkFields(Class classOfT, String regex );
    
    /**
     * 默认的反射方式的RecordAdapter
     */
    public static RecordAdapter sDefault = new RecordAdapter(){

        DaoConfig config = DaoConfig.getConfig();
        
        @Override
        public ContentValues createRecord( Object t ,String regex) {
            Entity entity = config.getEntityHolder().getEntityBy(t);
            return entity.getValues(t, regex);
        }

        @Override
        public String getCreateSql(Class classOf) {
            Entity<?> entity = config.getEntityHolder().getEntity(classOf);
            StringBuilder builder = new StringBuilder();
            List<MappingField> list = entity.getMappingFields();
            int size = list.size();
            MappingField mf;
            for (int i = 0; i < size; i++) {
                mf = list.get(i);
                builder.append(mf.getColumnName()).append(" ").append(mf.getColumnType().value).append(" ");
                if(mf.isNotNull()){
                    builder.append("NOT NULL ");
                }
                if(entity.getPkType() != PkType.COMPOSITE){
                    if(mf.isId() ){
                        builder.append(" PRIMARY KEY ");
                        if(mf.isAutoIncreasement()){
                            builder.append("AUTOINCREMENT ");
                        }
                    }else if(mf.isName()){
                        builder.append(" PRIMARY KEY ");
                    }
                }
                if(i != size -1){
                    builder.append(',');
                }
            }
            if(entity.getPkType() == PkType.COMPOSITE){
                builder.append("PRIMARY KEY (");
                list = entity.getCompositePKFields();
                size = list.size();
                for (int j = 0; j < size; j++) {
                    mf = list.get(j);
                    builder.append(mf.getColumnName());
                    if(j != size -1){
                        builder.append(",");
                    }
                }
                builder.append(')');
            }
            return builder.toString();
        }

        public Object bronObject(Class classOfT){
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            return entity.bronObject();
        }
        
        @Override
        public Object getObject( Cursor cs, Class classOfT ,Object t) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            return entity.getObject(cs,t);
        }

        @Override
        public String getTableName( Class classOfT ) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            return entity.getTableName();
        }

        @Override
        public PkType getPkType(Class classOfT) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            return entity.getPkType();
        }

        @Override
        public Map getPkNameAndValues(Object object, Class classOfT ) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            Map<String, Object> map;
            switch (getPkType(classOfT)) {
            case ID:
                map = new HashMap<>(1);
                map.put(entity.getIdField().getColumnName(),entity.getIdField().getValue(object));
                return map;
            case NAME:
                map = new HashMap<>(1);
                map.put(entity.getNameField().getColumnName(),entity.getNameField().getValue(object));
                return map;
            case COMPOSITE:
                List<MappingField> l =entity.getPks();
                map = new HashMap<>(l.size());
                for (MappingField mappingField : l) {
                    map.put(mappingField.getColumnName(),mappingField.getValue(object));
                }
                return map;
            default:
                return null;
            }
           
        }

        @Override
        public Object getPkNames( Class classOfT ) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            Map<String, Object> map;
            switch (getPkType(classOfT)) {
            case ID:
                return entity.getIdField().getColumnName();
            case NAME:
                return entity.getNameField().getColumnName();
            case COMPOSITE:
                List<MappingField> l =entity.getPks();
                String[] names = new String[l.size()];
                for (int i = 0; i < names.length; i++) {
                    names[i] = l.get(i).getColumnName();
                }
                return names;
            default:
                return null;
            }
        }

        @Override
        public List<LinkField> getOneLinkFields(Class classOfT ,String regex ) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            return entity.getOneLinkFields(regex);
        }

        @Override
        public List<LinkField> getManyLinkFields(Class classOfT ,String regex ) {
            Entity entity = config.getEntityHolder().getEntity(classOfT);
            return entity.getManyLinkFields(regex);
        }
    };

}
