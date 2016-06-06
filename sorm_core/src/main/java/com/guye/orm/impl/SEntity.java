package com.guye.orm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.guye.orm.Entity;
import com.guye.orm.LinkField;
import com.guye.orm.MappingField;
import com.guye.orm.PkType;
import com.guye.orm.utils.Lang;
import com.guye.orm.utils.Mirror;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

/**
 * 记录一个实体
 * 
 */
public class SEntity<T> implements Entity<T> {

    /**
     * 按照 Java 字段名索引映射字段
     */
    private Map<String, MappingField> byJava;

    /**
     * 按照数据库字段名索引映射字段
     */
    private Map<String, MappingField> byDB;

    /**
     * 按照增加顺序，记录所有映射字段
     */
    private List<MappingField>        fields;

    /**
     * 按顺序记录复合主键
     */
    private List<MappingField>        theComposites;

    /**
     * 所有一对一映射字段
     */
    protected List<LinkField>         ones;

    /**
     * 所有一对多映射字段
     */
    protected List<LinkField>         manys;

    /**
     * 数字型主键
     */
    private MappingField              theId;

    /**
     * 字符型主键
     */
    private MappingField              theName;

    /**
     * 实体 Java 类型
     */
    protected Class<T>                type;

    protected Mirror<T>               mirror;

    /**
     * 实体表名
     */
    private String                    tableName;

    /**
     * 实体的主键类型
     */
    private PkType                    pkType;

    private boolean                   complete;

    public SEntity(final Class<T> type) {
        this.type = type;
        this.mirror = Mirror.me(type);
        this.byJava = new HashMap<String, MappingField>();
        this.byDB = new HashMap<String, MappingField>();
        this.fields = new ArrayList<MappingField>(5);
        this.theComposites = new ArrayList<MappingField>(3);

        this.pkType = PkType.UNKNOWN;

        // 映射
        this.ones = new ArrayList<LinkField>();
        this.manys = new ArrayList<LinkField>();
    }

    public T bronObject(){
       return mirror.born();
    }
    
    public T getObject( Cursor rs ,T object) {
        // 通过反射每个字段逐次设置对象
        for (MappingField fld : fields) {
            fld.injectValue(object, rs);
        }
        // 返回构造的对象
        return object;
    }

    public T getObject( ContentValues rec ,T object) {
        for (MappingField fld : fields)
            fld.injectValue(object, rec);
        return object;

    }

    public ContentValues getValues( T t, String regex ) {
        ContentValues contentValues = new ContentValues();
        for (MappingField fld : fields)
            if (TextUtils.isEmpty(regex) || fld.getName().matches(regex)) {
                fld.enjectValue(t, contentValues);
            }

        return contentValues;
    }

    /**
     * 当所有字段增加完成，这个方法必须被调用，用来搜索复合主键
     * 
     * @param names
     *            复合主键的 Java 字段名数组
     */
    public void checkCompositeFields( String[] names ) {
        if (!Lang.isEmpty(names) && names.length > 1) {
            for (String name : names) {
                if (byJava.containsKey(name) && byJava.get(name).isCompositePk())
                    theComposites.add(byJava.get(name));
                else
                    throw new RuntimeException(
                            String.format("Fail to find comosite field '%s' in class '%s'!", name,
                                    type.getName()));
            }
            this.pkType = PkType.COMPOSITE;
        } else if (null != this.theId) {
            this.pkType = PkType.ID;
        } else if (null != this.theName) {
            this.pkType = PkType.NAME;
        }
    }

    /**
     * 增加映射字段
     * 
     * @param field
     *            数据库实体字段
     */
    public void addMappingField( MappingField field ) {
        if (field.isId())
            theId = field;
        else if (field.isName())
            theName = field;
        byJava.put(field.getName(), field);
        byDB.put(field.getColumnName(), field);
        fields.add(field);
    }

    /**
     *
     * @param lnk
     */
    public void addLinkField( LinkField lnk ) {
        switch (lnk.getLinkType()) {
        case LinkField.ONE:
            ones.add(lnk);
            break;
        case LinkField.MANY:
            manys.add(lnk);
            break;
        default:
            throw new RuntimeException(String.format("It is a miracle in Link field: '%s'(%s)",
                    lnk.getName(), getType().getName()));
        }
    }

    @Override
    public List<LinkField> getOneLinkFields( String regex ) {
        ArrayList<LinkField> fields = new ArrayList<LinkField>(ones.size());
        for (LinkField linkField : ones) {
            if(TextUtils.isEmpty(regex)|| linkField.getName().matches(regex)){
                fields.add(linkField);
            }
        }
        return fields;
    }

    @Override
    public List<LinkField> getManyLinkFields( String regex ) {
        ArrayList<LinkField> fields = new ArrayList<LinkField>(manys.size());
        for (LinkField linkField : manys) {
            if(TextUtils.isEmpty(regex)|| linkField.getName().matches(regex)){
                fields.add(linkField);
            }
        }
        return fields;
    }

    public void setTableName( String namep ) {
        this.tableName = namep;
    }

    public MappingField getField( String name ) {
        return byJava.get(name);
    }

    public MappingField getColumn( String name ) {
        return byDB.get(name);
    }

    public List<MappingField> getMappingFields() {
        return fields;
    }

    public List<LinkField> getLinkFields() {
        List<LinkField> re = new ArrayList<LinkField>(ones.size() + ones.size());
        re.addAll(ones);
        re.addAll(manys);
        return re;
    }

    public List<MappingField> getCompositePKFields() {
        return this.theComposites;
    }

    public MappingField getNameField() {
        return this.theName;
    }

    public MappingField getIdField() {
        return this.theId;
    }

    public List<MappingField> getPks() {
        if (null != theId)
            return Lang.list(theId);
        if (null != theName)
            return Lang.list(theName);
        return theComposites;
    }

    public Class<T> getType() {
        return this.type;
    }

    public String getTableName() {
        return this.tableName;
    }

    public PkType getPkType() {
        return pkType;
    }

    public String toString() {
        return String.format("Entity<%s:%s>", getType().getName(), getTableName());
    }

    public boolean isComplete() {
        return complete;
    }

    void setComplete( boolean complete ) {
        this.complete = complete;
    }

    @Override
    public Mirror<T> getMirror() {
        return mirror;
    }

}
