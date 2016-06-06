package com.guye.orm.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.guye.orm.ColumnAdapter;
import com.guye.orm.DaoConfig;
import com.guye.orm.DaoException;
import com.guye.orm.Entity;
import com.guye.orm.LinkField;
import com.guye.orm.MappingField;
import com.guye.orm.PkType;
import com.guye.orm.annotation.ColAdapter;
import com.guye.orm.annotation.ColType;
import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Id;
import com.guye.orm.annotation.Many;
import com.guye.orm.annotation.Name;
import com.guye.orm.annotation.One;
import com.guye.orm.annotation.PK;
import com.guye.orm.annotation.Table;
import com.guye.orm.utils.Lang;
import com.guye.orm.utils.Mirror;

import android.text.TextUtils;

/**
 * @author nieyu
 *
 */
public class AnnotationEntityMaker  {

    private EntityHolder holder;
    
    protected AnnotationEntityMaker() {
	}
    
    public void init(EntityHolder holder) {
        this.holder = holder;
    }

    public AnnotationEntityMaker( EntityHolder holder) {
        init( holder);
    }

    public <T> Entity<T> make(Class<T> type) {
        SEntity<T> en = _createNutEntity(type);

        TableInfo ti = _createTableInfo(type);
        if(ti.annTable == null){
            throw new RuntimeException("can find Table Annotation on Class: " + type.getName());
        }
        /*
         * 获得表名以及视图名称及注释
         */
        String tableName = null;
        if (TextUtils.isEmpty(ti.annTable.value())) {
        	tableName = Lang.lowerWord(type.getSimpleName(), '_');
        } else {
        	tableName = ti.annTable.value();
        }
        en.setTableName(tableName);
        String[] _tmp = ti.annPK == null ? null : ti.annPK.value();
        List<String> pks = _tmp == null ? new ArrayList<String>() : Arrays.asList(_tmp);
        Field[] fields = en.getMirror().getFields();
        /*
         * 获取所有的数据库字段
         */
        // 字段里面是不是有声明过 '@Column' @Comment
        for (Field field : fields) {
            // 应该忽略
            if ((Modifier.isTransient(field.getModifiers()) && null == field.getAnnotation(Column.class))
                    || ((null == field.getAnnotation(Column.class)
                                            && null == field.getAnnotation(Id.class) && null == field.getAnnotation(Name.class)))
                                            && !pks.contains(field.getName())) {
               continue;
           }
           // '@Column'
           else {
               MappingFieldImpl mappingField = new MappingFieldImpl(en, field);
               _evalMappingField(mappingField, field, ti.annPK);
               en.addMappingField(mappingField);
           }
        }
        en.checkCompositeFields(_tmp);
        for (Field field : fields) {
            // '@One'
            if (null != field.getAnnotation(One.class)) {
                LinkFieldImpl linkField = new LinkFieldImpl(en, field);
                _evalOneLinkField(linkField, field);
                en.addLinkField(linkField);
            }
            // '@Many'
            else if (null != field.getAnnotation(Many.class)) {
                LinkFieldImpl linkField = new LinkFieldImpl(en, field);
                _evalManyLinkField(linkField, field);
                en.addLinkField(linkField);
            }
        }

       

        // 搞定收工，哦耶 ^_^
        en.setComplete(true);
        return en;
    }

    /**
     * 向父类递归查找实体的配置
     * 
     * @param type
     *            实体类型
     * @return 实体表描述
     */
    private TableInfo _createTableInfo(Class<?> type) {
        TableInfo info = new TableInfo();
        info.annTable = type.getAnnotation(Table.class);
        info.annPK = type.getAnnotation(PK.class);
        return info;
    }

    /**
     * @param ef
     */
    private void _evalMappingField(MappingFieldImpl ef, Field field , PK annPK) {
        Column annColumn = field.getAnnotation(Column.class);
        Id annId = field.getAnnotation(Id.class);
        Name annName = field.getAnnotation(Name.class);
        ColAdapter annAdapter = field.getAnnotation(ColAdapter.class);
        //检查@Id和@Name的属性类型
        if (annId != null) {
            if (!Mirror.me(field.getType()).isLong())
                throw new DaoException(String.format("Field(%s) annotation @Id , but not Number type!!", field));
        }
        
        if (annName != null)
            if (!Mirror.me(field.getType()).isStringLike())
                throw new DaoException(String.format("Field(%s) annotation @Name , but not String type!!", field));
        // 字段的 Java 名称
        ef.setType(field.getGenericType());
        String columnName = "";
        // 字段的数据库名
        if (null == annColumn || TextUtils.isEmpty(annColumn.value())){
            columnName = field.getName();
        }else {
            columnName = annColumn.value();

        }
        if(null != annColumn){
            ef.useGetAndSet = annColumn.useGetAndSet();
            if(annColumn.isNotNull())
                ef.setAsNotNull();
        }
        ef.setColumnName(columnName);

        // Id 字段
        if (null != annId) {
            ef.setAsId();
            if (annId.auto()) {
                ef.setAsAutoIncreasement();
            }
        }

        // Name 字段
        if (null != annName) {
            ef.setAsName();
        }

        // 检查 @Id 和 @Name 的冲突
        if (ef.isId() && ef.isName())
            throw new RuntimeException(String.format("Field '%s'(%s) can not be @Id and @Name at same time!",
                                 ef.getName(),
                                 ef.getEntity().getType().getName()));

        // 检查 PK
        if (null != annPK) {
            // 用 @PK 的方式声明的主键
            if (annPK.value().length == 1) {
                if (Lang.contains(annPK.value(), field.getName())) {
                    if (ef.getTypeMirror().isIntLike())
                        ef.setAsId();
                    else
                        ef.setAsName();
                }
            }
            // 看看是不是复合主键
            else if (Lang.contains(annPK.value(),  field.getName()))
                ef.setAsCompositePk();
        }

        //处理类型
        if(annAdapter != null && annAdapter.value() != null){
            Class<? extends ColumnAdapter> cz = (Class<? extends ColumnAdapter>) annAdapter.value();
            ColumnAdapter adapter = DaoConfig.getConfig().getAdapter(cz.getName());
            ef.setColumnType(annAdapter.type());
            ef.setAdaptor(adapter);
        }else{
            Mirror<?> mirror = ef.getTypeMirror();
            if(mirror.isDateTimeLike()){
                ef.setColumnType(ColType.TEXT);
                ef.setAdaptor(ColumnAdapterDefault.dateAdapter);
            }else if(mirror.isIntLike() || mirror.isChar()){
                ef.setColumnType(ColType.INTEGER);
                ef.setAdaptor(ColumnAdapterDefault.intAdapter);
            }else if(mirror.isDecimal()){
                ef.setColumnType(ColType.REAL);
                ef.setAdaptor(ColumnAdapterDefault.realAdapter);
            }else if(mirror.isStringLike()){
                ef.setColumnType(ColType.TEXT);
                ef.setAdaptor(ColumnAdapterDefault.textAdapter);
            }else if(mirror.isArray() && (ef.getTypeClass().getComponentType()==byte.class ||  ef.getTypeClass().getComponentType()==Byte.class)){
                ef.setColumnType(ColType.BLOB);
                ef.setAdaptor(ColumnAdapterDefault.blobAdapter);
            }else if(mirror.isEnum()){
                ef.setColumnType(ColType.INTEGER);
                ef.setAdaptor(ColumnAdapterDefault.enumAdapter);
            }else if(mirror.isBoolean()){
                ef.setColumnType(ColType.TEXT);
                ef.setAdaptor(ColumnAdapterDefault.booleanAdapter);
            }else{
                throw new IllegalArgumentException(String.format("can not adapter field %s , in class %",ef.getName(),ef.getEntity().getClass().getName()));
            }
        }

    }
    
    private void _evalOneLinkField(LinkFieldImpl ef, Field field) {
     // 字段的 Java 名称
        ef.setType(field.getGenericType());
        ef.setLinkType(LinkField.ONE);
        One one = field.getAnnotation(One.class);
        Class<?> tatget = one.target();
        String fieldname = one.field();
        ef.setTargetType(tatget);
        Entity<?> entity = holder.getEntity(tatget);
        ef.setTarget(entity);
        MappingField mappingField = ef.getEntity().getField(fieldname);
        if(entity.getPkType() !=PkType.ID && entity.getPkType() != PkType.NAME){
            throw new IllegalArgumentException("target table mast has a id key or name key");
        }
        ef.setLinkedAndHostField(entity.getPkType()==PkType.ID?entity.getIdField():entity.getNameField(), mappingField);
       
    }
    private void _evalManyLinkField(LinkFieldImpl ef, Field field) {
     // 字段的 Java 名称
        ef.setType(field.getGenericType());
        ef.setLinkType(LinkField.MANY);
        Many many = field.getAnnotation(Many.class);
        Class<?> tatget = many.target();
        String fieldname = many.field();
        ef.setTargetType(tatget);
        Entity<?> entity = holder.getEntity(tatget);
        ef.setTarget(entity);
        if(ef.getEntity().getPkType() !=PkType.ID && ef.getEntity().getPkType() != PkType.NAME){
            throw new IllegalArgumentException("target table mast has a id key or name key");
        }
        MappingField mappingField = ef.getEntity().getPkType()==PkType.ID?ef.getEntity().getIdField():ef.getEntity().getNameField();
        ef.setLinkedAndHostField(entity.getField(fieldname), mappingField);
    }

    protected <T> SEntity<T> _createNutEntity(Class<T> type) {
        return new SEntity<T>(type);
    }
    
    static class TableInfo {

        public Table annTable;

        public PK annPK;

    }
}
