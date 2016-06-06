package com.guye.orm.apt.type;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import com.guye.orm.apt.ColAdapterHolder;
import com.guye.orm.apt.utils.Lang;
import com.guye.orm.apt.utils.Constants;

import com.google.auto.common.MoreTypes;
import com.guye.orm.annotation.ColType;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.processing.Messager;

/**
 * Created by nieyu on 16/5/17.
 */
public class ColumnInfo {

    private TypeElement typeElement;

    private VariableElement variableElement;

    private ExecutableElement geterElement;

    private ExecutableElement seterElement;

    private String columnName;

    private ColType columnType;

    private boolean isCompositePk;

    private boolean isId;

    private boolean isName;

    private boolean notNull;

    private boolean autoIncreasement;

    private TypeMirror type;

    private String name;

    private String adapter;

    private String cursorGetOpt;

    private boolean useGetAndSet;
    
    private Messager message;

    public ColumnInfo(Messager msg) {
        message = msg;
    }

    public void setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setColumnType(ColType columnType) {
        this.columnType = columnType;
    }

    public void setCompositePk(boolean compositePk) {
        isCompositePk = compositePk;
    }

    public void setId(boolean id) {
        isId = id;
        
    }

    public void setName(boolean name) {
        isName = name;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public void setAutoIncreasement(boolean autoIncreasement) {
        this.autoIncreasement = autoIncreasement;
    }

    public void setType(TypeMirror type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }

    public void setUseGetAndSet(boolean useGetAndSet) {
        this.useGetAndSet = useGetAndSet;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }

    public String getColumnName() {
        return columnName;
    }

    public ColType getColumnType() {
        return columnType;
    }

    public boolean isCompositePk() {
        return isCompositePk;
    }

    public boolean isId() {
        return isId;
    }

    public boolean isName() {
        return isName;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isAutoIncreasement() {
        return autoIncreasement;
    }

    public TypeMirror getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getAdapter() {
        return adapter;
    }

    public boolean isUseGetAndSet() {
        return useGetAndSet;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public ExecutableElement getGeterElement() {
        return geterElement;
    }

    public ExecutableElement getSeterElement() {
        return seterElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public void setGeterElement(ExecutableElement geterElement) {
        this.geterElement = geterElement;
    }

    public void setSeterElement(ExecutableElement seterElement) {
        this.seterElement = seterElement;
    }

    public String getCursorGetOpt() {
        return cursorGetOpt;
    }

    public void setCursorGetOpt(String cursorGetOpt) {
        this.cursorGetOpt = cursorGetOpt;
    }

    public CodeBlock getContentValuesGetCode(String cvArg , String objArg){
        CodeBlock.Builder cb = CodeBlock.builder();
        if(getAdapter() != null){
            TypeElement typeElement = ColAdapterHolder.getAdapter(getAdapter());
            cb.addStatement(Constants.CORE_PKGNAME+".ColumnAdapter "+getName()+"Adapter="+Constants.CORE_PKGNAME+".DaoConfig.getConfig().getAdapter($S)",getAdapter());
            String temp = "$L.set($L.class,$S,$L,$L)";
            cb.addStatement(temp,getName()+"Adapter",getType().toString(),getColumnName(),getGetFromObjectCode(objArg),cvArg);
        }else if(MoreTypes.isTypeOf(char.class, getType())){
            String temp = "$L.put($S,(short)";
            cb.addStatement(temp+getGetFromObjCodeWithType(getGetFromObjectCode(objArg))+")",cvArg,getColumnName());
        }else if(MoreTypes.isTypeOf(Character.class, getType())){
            String temp = "$L.put($S,(short)(char)";
            cb.addStatement(temp+getGetFromObjCodeWithType(getGetFromObjectCode(objArg))+")",cvArg,getColumnName());
        }else{
            String temp = "$L.put($S,";
            cb.addStatement(temp+getGetFromObjCodeWithType(getGetFromObjectCode(objArg))+")",cvArg,getColumnName());
        }
        return cb.build();
    }

    public CodeBlock getCursorGetCode(String curArg , String indexArg ){
        CodeBlock.Builder cb = CodeBlock.builder();
        String curGetMethod = getCursorGetOpt();
        if(getAdapter() != null){
            TypeElement typeElement = ColAdapterHolder.getAdapter(getAdapter());
            cb.addStatement(Constants.CORE_PKGNAME+".ColumnAdapter "+getName()+"Adapter="+Constants.CORE_PKGNAME+".DaoConfig.getConfig().getAdapter($S)",getAdapter());
            String temp =  "$L $L=($L) $L.get($L.class,$S,$L)";
            cb.addStatement(temp,getType().toString(),getName(),getType().toString(),getName()+"Adapter",getType().toString(),getColumnName(),curArg);
        }else{
            String temp;
            if(MoreTypes.isTypeOf(boolean.class, getType()) || MoreTypes.isTypeOf(Boolean.class, getType())){
                temp ="boolean $L=java.lang.Boolean.parseBoolean($L.getString($L))";
                cb.addStatement(temp , getName(),curArg , indexArg);
            }else if(MoreTypes.isTypeOf(char.class, getType()) || MoreTypes.isTypeOf(Character.class, getType())){
                temp ="$L $L = (char)$L.$L($L)";
                cb.addStatement(temp,getType().toString(),getName(),curArg,getCursorGetOpt(),indexArg);
            }else if(MoreTypes.isTypeOf(Byte.class, getType()) || MoreTypes.isTypeOf(byte.class, getType())){
                temp = "$L $L = (byte)$L.$L($L)";
                cb.addStatement(temp,getType().toString(),getName(),curArg,getCursorGetOpt(),indexArg);
            }else if(MoreTypes.isTypeOf(Date.class, getType()) ){
                temp = "java.util.Date $L = new java.util.Date($L.getLong($L))";
                cb.addStatement(temp,getName(),curArg,indexArg);
            }else if(MoreTypes.isTypeOf(Calendar.class, getType()) ){
                temp = "java.util.Calendar $L = java.util.Calendar.getInstance();$L.setTimeInMillis($L.getLong($L))";
                cb.addStatement(temp,getName(),getName(),curArg,indexArg);
            }else if(getType().getKind() == TypeKind.DECLARED){
                TypeMirror superType = ((TypeElement)((DeclaredType)getType()).asElement()).getSuperclass();
                if(MoreTypes.isTypeOf(Enum.class,superType)) {
                    temp = "$L $L=$L.values()[$L.getInt($L)]";
                    cb.addStatement(temp, getType().toString(), getName(), getType().toString(), curArg, indexArg);
                }else{
                    temp = "$L $L = $L.$L($L)";
                    cb.addStatement(temp,getType().toString(),getName(),curArg,getCursorGetOpt(),indexArg);
                }
            }else{
                temp = "$L $L = $L.$L($L)";
                cb.addStatement(temp,getType().toString(),getName(),curArg,getCursorGetOpt(),indexArg);
            }
        }
        return cb.build();
    }

    public CodeBlock getSet2ObjectCode(String ojbArg , String valueArg){
        CodeBlock.Builder cb = CodeBlock.builder();
        Set<Modifier> modifier = variableElement.getModifiers();
        String curGetMethod = getCursorGetOpt();
        if(useGetAndSet && (getSeterElement() != null && !Lang.isEmpty(getSeterElement().getSimpleName().toString()))){
            cb.addStatement(ojbArg+"."+getSeterElement().getSimpleName()+"("+valueArg+")");
        }else if(!modifier.contains(Modifier.PRIVATE) && !modifier.contains(Modifier.PROTECTED)){
            cb.addStatement(ojbArg+"."+variableElement.getSimpleName()+"="+valueArg);
        }else{
            message.printMessage(Kind.ERROR, variableElement.getSimpleName()+" not public and have no set method", typeElement);
        }

        return cb.build();
    }

    public CodeBlock getGetFromObjectCode(String objArg){
        CodeBlock.Builder cbInner = CodeBlock.builder();
        Set<Modifier> modifier = variableElement.getModifiers();
        if(useGetAndSet && (getGeterElement() != null && !Lang.isEmpty(getGeterElement().getSimpleName().toString()))){
                cbInner.add("$L.$L()",objArg,getGeterElement().getSimpleName().toString());

        }else if(!modifier.contains(Modifier.PRIVATE) && !modifier.contains(Modifier.PROTECTED)){
            cbInner.add("$L.$L",objArg,getName());
        }else{
            message.printMessage(Kind.ERROR, variableElement.getSimpleName()+" not public and have no get method", typeElement);
        }

        return cbInner.build();
    }

    public CodeBlock getGetFromObjCodeWithType( CodeBlock cbInner ) {
        CodeBlock.Builder cb = CodeBlock.builder();

        String temp;
        if(MoreTypes.isTypeOf(boolean.class, getType()) || MoreTypes.isTypeOf(Boolean.class, getType())){
            temp ="java.lang.String.valueOf(";
            cb.add(temp +cbInner+")");
        }else if(MoreTypes.isTypeOf(Date.class, getType()) ){
            temp = ".getTime()";
            cb.add(cbInner+temp);
        }else if(MoreTypes.isTypeOf(Calendar.class, getType()) ){
            temp = ".getTimeInMillis()";
            cb.add(cbInner+temp);
        }else if(getType().getKind() == TypeKind.DECLARED){
            TypeMirror superType = ((TypeElement)((DeclaredType)getType()).asElement()).getSuperclass();
            if(MoreTypes.isTypeOf(Enum.class,superType)) {
                temp = ".ordinal()";
                cb.add(cbInner+temp);
            }else{
                return cbInner;
            }
        }else{
            return cbInner;
        }

        return cb.build();
    }

    public CodeBlock getCreateSqlCode(String buildArg) {
        CodeBlock.Builder block = CodeBlock.builder();
        String temp ="$L.append($S).append(\" \").append($S)";
        
        block.addStatement(temp, buildArg, getColumnName() , getColumnType().value);
        if(isNotNull()){
            block.addStatement("$L.append(\"NOT NULL \")", buildArg);
        }
        if(!isCompositePk){
            if(isId || isName){
                if(isAutoIncreasement()){
                    block.addStatement("$L.append(\"AUTOINCREMENT \")", buildArg);
                }
                block.addStatement("$L.append(\" PRIMARY KEY \")", buildArg);
            }
            
        }
        return block.build();
    }
}
