package com.guye.orm.apt.type;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import android.content.ContentValues;
import android.database.Cursor;
import com.guye.orm.Condition;
import com.guye.orm.apt.utils.Constants;
import com.guye.orm.apt.utils.Lang;
import com.guye.orm.impl.TemplateLinkField;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

/**
 * Created by nieyu on 16/5/17.
 */
public class EntityInfo {

    /**
     * 按照 Java 字段名索引映射字段
     */
    private Map<String, ColumnInfo> byJava        = new HashMap<>();

    /**
     * 按照数据库字段名索引映射字段
     */
    private Map<String, ColumnInfo> byDB          = new HashMap<>();

    private TypeMirror              superClass;

    private TypeElement             typeElement;

    private String                  tableName;

    private List<ColumnInfo>        columnInfos   = new ArrayList<>();
    private List<LinkInfo>          ones          = new ArrayList<>();
    private List<LinkInfo>          manys         = new ArrayList<>();

    private List<ColumnInfo>        theComposites = new ArrayList<ColumnInfo>();

    private Messager msg;

    public EntityInfo(Messager msg) {
        this.msg = msg;
    }

    public void addCompositePK( ColumnInfo ci ) {
        theComposites.add(ci);
    }

    /**
     * 实体的主键类型
     */
    private PkType     pkType;

    /**
     * 数字型主键
     */
    private ColumnInfo theId;

    /**
     * 字符型主键
     */
    private ColumnInfo theName;

    private String binName;

    public void addColumnInfo( ColumnInfo columnInfo ) {
        if (columnInfo.isId())
            theId = columnInfo;
        else if (columnInfo.isName())
            theName = columnInfo;
        byJava.put(columnInfo.getName(), columnInfo);
        byDB.put(columnInfo.getColumnName(), columnInfo);
        columnInfos.add(columnInfo);
    }

    
    public List<ColumnInfo> getColumnInfos() {
        return columnInfos;
    }

    public List<LinkInfo> getOnes() {
        return ones;
    }

    public List<LinkInfo> getManys() {
        return manys;
    }

    /**
     *
     * @param lnk
     */
    public void addLinkField( LinkInfo lnk ) {
        switch (lnk.getLinkType()) {
        case LinkInfo.ONE:
            ones.add(lnk);
            break;
        case LinkInfo.MANY:
            manys.add(lnk);
            break;
        default:
            throw new RuntimeException();
        }
    }

    public List<LinkInfo> getOneLinkFields( String regex ) {
        ArrayList<LinkInfo> fields = new ArrayList<LinkInfo>(ones.size());
        for (LinkInfo linkField : ones) {
            if (Lang.isEmpty(regex) || linkField.getName().matches(regex)) {
                fields.add(linkField);
            }
        }
        return fields;
    }

    public List<LinkInfo> getManyLinkFields( String regex ) {
        ArrayList<LinkInfo> fields = new ArrayList<LinkInfo>(manys.size());
        for (LinkInfo linkField : manys) {
            if (Lang.isEmpty(regex) || linkField.getName().matches(regex)) {
                fields.add(linkField);
            }
        }
        return fields;
    }

    public TypeMirror getSuperClass() {
        return superClass;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnInfo getField( String name ) {
        return byJava.get(name);
    }

    public ColumnInfo getColumn( String name ) {
        return byDB.get(name);
    }

    public List<ColumnInfo> getTheComposites() {
        return theComposites;
    }

    public PkType getPkType() {
        return pkType;
    }

    public ColumnInfo getTheId() {
        return theId;
    }

    public ColumnInfo getTheName() {
        return theName;
    }

    public void setSuperClass( TypeMirror superClass ) {
        this.superClass = superClass;
    }

    public void setTypeElement( TypeElement typeElement ) {
        this.typeElement = typeElement;
    }

    public void setTableName( String tableName ) {
        this.tableName = tableName;
    }

    public void setPkType( PkType pkType ) {
        this.pkType = pkType;
    }

    public void setTheId( ColumnInfo theId ) {
        this.theId = theId;
    }

    public void setTheName( ColumnInfo theName ) {
        this.theName = theName;
    }
    
    public String getBinName() {
        return binName;
    }

    public void setBinName( String binName ) {
        this.binName = binName;
    }

}
