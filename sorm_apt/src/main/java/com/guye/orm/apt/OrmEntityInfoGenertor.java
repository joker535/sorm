package com.guye.orm.apt;

import static com.guye.orm.annotation.ColType.BLOB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import com.guye.orm.apt.type.ColumnInfo;
import com.guye.orm.apt.type.EntityInfo;
import com.guye.orm.apt.type.LinkInfo;
import com.guye.orm.apt.type.PkType;
import com.guye.orm.apt.utils.Lang;

import com.google.auto.common.MoreTypes;
import com.guye.orm.annotation.ColAdapter;
import com.guye.orm.annotation.ColType;
import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.Id;
import com.guye.orm.annotation.Many;
import com.guye.orm.annotation.Name;
import com.guye.orm.annotation.One;
import com.guye.orm.annotation.PK;
import com.guye.orm.annotation.Table;

/**
 * Created by nieyu on 16/5/19.
 * 生成TableInfo的工具类
 */
public class OrmEntityInfoGenertor {

    public OrmEntityInfoGenertor(ProcessingEnvironment processingEnv , Messager msg) {
        this.processingEnv = processingEnv;
        this.msg = msg;
    }

    private ProcessingEnvironment processingEnv;
    private Messager msg;
    private static List<String> StringLike = new ArrayList<>();
    static {
        StringLike.add("java.lang.CharSequence");
        StringLike.add("java.lang.String");
    }

    /**
     * @param typeElement
     * 生成Talbeinfo
     */
    public EntityInfo make(TypeElement typeElement){
//        msg.printMessage(Kind.NOTE, "start on:"+typeElement.getQualifiedName().toString());
        EntityInfo entityInfo = new EntityInfo(msg);
        entityInfo.setTypeElement(typeElement);
        entityInfo.setBinName(processingEnv.getElementUtils().getBinaryName(typeElement).toString());
        entityInfo.setSuperClass(typeElement.getSuperclass());


        TableInfo ti = _createTableInfo(typeElement);
        //得到表名
        String tableName = null;
        if (Lang.isEmpty(ti.annTable.value())) {
            tableName = Lang.lowerWord(typeElement.getSimpleName(), '_');
        } else {
            tableName = ti.annTable.value();
        }
        entityInfo.setTableName(tableName);
        //是否是组合主键
        String[] _tmp = ti.annPK == null ? null : ti.annPK.value();
        List<String> pks = _tmp == null ? new ArrayList<String>() : Arrays.asList(_tmp);

        //先生成包含Column注解的列的columninfo
        List<? extends Element> elements = typeElement.getEnclosedElements();
        for (Element ele2 : elements) {
            if (ele2 instanceof VariableElement && !ele2.getModifiers().contains(Modifier.STATIC)) {
                if(ele2.getAnnotation(Column.class) != null || ele2.getAnnotation(Id.class) != null || ele2.getAnnotation(Name.class) != null){
                    VariableElement variableElement = (VariableElement) ele2;
                    ColumnInfo ci = new ColumnInfo(msg);
                    entityInfo.addColumnInfo(generteColInfo(entityInfo,ci ,typeElement , variableElement , ti.annPK));
                }
            }
        }
        //生成包含One和Many注解的列的linkinfo
        for (Element ele2 : elements) {
            if (ele2 instanceof VariableElement) {
                if(ele2.getAnnotation(One.class) != null){
                    VariableElement variableElement = (VariableElement) ele2;
                    entityInfo.addLinkField(generteOneLinkInfo(typeElement,variableElement,entityInfo));
                }else if( ele2.getAnnotation(Many.class) != null){
                    VariableElement variableElement = (VariableElement) ele2;
                    entityInfo.addLinkField(generteManyLinkInfo(typeElement,variableElement,entityInfo));
                }
            }
        }
//        msg.printMessage(Kind.NOTE, "end on:"+typeElement.getQualifiedName().toString());
        return entityInfo;
    }
    /**
     * @param type
     * 获取Table和PK注解
     */
    private TableInfo _createTableInfo(TypeElement type) {
        TableInfo info = new TableInfo();

        info.annTable = type.getAnnotation(Table.class);
        info.annPK = type.getAnnotation(PK.class);
        return info;
    }
    /**
     * @param ci 要填充的ColumnInfo
     * @param entityInfo 对应的类信息
     * @param ve 对应字段的Element
     * 填充的ColumnInfo
     */
    private ColumnInfo generteColInfo(EntityInfo entityInfo,ColumnInfo ci,TypeElement classElement, VariableElement ve, PK annPK){
        Column annColumn = ve.getAnnotation(Column.class);
        Id annId = ve.getAnnotation(Id.class);
        Name annName = ve.getAnnotation(Name.class);
        ColAdapter annAdapter = ve.getAnnotation(ColAdapter.class);

        ci.setTypeElement(classElement);

        //检查@Id和@Name的属性类型
        if (annId != null) {
            if (ve.asType().getKind()!= TypeKind.LONG)
                throw new RuntimeException(String.format("Field(%s) annotation @Id , but not Number type!!", ve));
        }

        String type = ve.asType().toString();
        TypeMirror tm = ve.asType();
        if (annName != null){
            if (!StringLike.contains(type))
                throw new RuntimeException(String.format("Field(%s) annotation @Name , but not String type!!", ve));
        }
        ci.setVariableElement(ve);
        // 字段的 Java 名称
        ci.setType(tm);
        ci.setName(ve.getSimpleName().toString());
        String columnName = "";
        // 字段的数据库名
        if (null == annColumn || Lang.isEmpty(annColumn.value())){
            columnName = ve.getSimpleName().toString();

        }else {
            columnName = annColumn.value();
        }
        if(null != annColumn){
            ci.setUseGetAndSet(annColumn.useGetAndSet());
            if(annColumn.isNotNull())
                ci.setNotNull(true);
        }
        ci.setColumnName(columnName);
        ci.setGeterElement(getGeter(classElement , ve));
        ci.setSeterElement(getSeter(classElement , ve));
        // Id 字段
        if (null != annId) {
            ci.setId(true);
            entityInfo.setPkType(PkType.ID);
            if (annId.auto()) {
                ci.setAutoIncreasement(true);
            }
        }

        // Name 字段
        if (null != annName) {
            ci.setName(true);
            entityInfo.setPkType(PkType.NAME);
        }

        // 检查 @Id 和 @Name 的冲突
        if (ci.isId() && ci.isName())
            throw new RuntimeException(String.format("Field '%s'(%s) can not be @Id and @Name at same time!",
                    ci.getName(),
                    ci.getType()));

        // 检查 PK
        if (null != annPK) {
            // 用 @PK 的方式声明的主键
            if (annPK.value().length == 1) {
                if (Lang.contains(annPK.value(), ci.getName())) {
                    if (ve.asType().getKind()!= TypeKind.LONG)
                        ci.setId(true);
                    else
                        ci.setName(true);
                }
            }
            // 看看是不是复合主键
            else if (Lang.contains(annPK.value(), ci.getName())){
                ci.setCompositePk(true);
                entityInfo.setPkType(PkType.COMPOSITE);
                entityInfo.addCompositePK(ci);
            }
        }

        //处理类型
        if(annAdapter != null && annAdapter.type() != null){
            ci.setColumnType(annAdapter.type());
            if(annAdapter.type() != null){
            }

            List<? extends AnnotationMirror> las = ve.getAnnotationMirrors();
            for (int i = 0 ; i<las.size();i++){
                AnnotationMirror anm = las.get(i);
                if(anm.getAnnotationType().toString().equals(ColAdapter.class.getName())){
                    Map<? extends ExecutableElement,? extends AnnotationValue> mapp = ve.getAnnotationMirrors().get(i).getElementValues();
                    for (ExecutableElement ee:
                            mapp.keySet()) {
                        if(ee.getSimpleName().toString().equals("value")){
                            ci.setAdapter(mapp.get(ee).getValue().toString());
                        }
                    }
                }
            }
        }else {
            if (MoreTypes.isTypeOf(int.class, tm) || MoreTypes.isTypeOf(Integer.class, tm)) {
                ci.setColumnType(ColType.INTEGER);
                ci.setCursorGetOpt("getInt");
            } else if (MoreTypes.isTypeOf(short.class, tm) || MoreTypes.isTypeOf(Short.class, tm) || MoreTypes.isTypeOf(char.class, tm) || MoreTypes.isTypeOf(Character.class, tm)) {
                ci.setColumnType(ColType.INTEGER);
                ci.setCursorGetOpt("getShort");
            } else if (MoreTypes.isTypeOf(long.class, tm) || MoreTypes.isTypeOf(Long.class, tm)) {
                ci.setColumnType(ColType.INTEGER);
                ci.setCursorGetOpt("getLong");
            } else if (MoreTypes.isTypeOf(byte.class, tm) || MoreTypes.isTypeOf(Byte.class, tm)) {
                ci.setColumnType(ColType.INTEGER);
                ci.setCursorGetOpt("getShort");
            } else if (MoreTypes.isTypeOf(float.class, tm) || MoreTypes.isTypeOf(Float.class, tm)) {
                ci.setColumnType(ColType.REAL);
                ci.setCursorGetOpt("getFloat");
            } else if (MoreTypes.isTypeOf(double.class, tm) || MoreTypes.isTypeOf(Double.class, tm)) {
                ci.setColumnType(ColType.REAL);
                ci.setCursorGetOpt("getDouble");
            } else if (MoreTypes.isTypeOf(boolean.class, tm) || MoreTypes.isTypeOf(Boolean.class, tm)) {
                ci.setColumnType(ColType.TEXT);
                ci.setCursorGetOpt("getString");
            } else if (MoreTypes.isTypeOf(Date.class, tm) || MoreTypes.isTypeOf(Calendar.class, tm)) {
                ci.setColumnType(ColType.INTEGER);
                ci.setCursorGetOpt("getInt");
            } else if (MoreTypes.isTypeOf(String.class, tm)) {
                ci.setColumnType(ColType.TEXT);
                ci.setCursorGetOpt("getString");
            } else if (tm.toString().equals("byte[]")){
                ci.setColumnType(BLOB);
                ci.setCursorGetOpt("getBlob");
            } else{
                if(tm.getKind() == TypeKind.DECLARED){
                    TypeMirror superType = ((TypeElement)((DeclaredType)tm).asElement()).getSuperclass();
                    if(superType instanceof NoType){
                        
                    }else
                    if(MoreTypes.isTypeOf(Enum.class,superType)){
                        ci.setColumnType(ColType.INTEGER);
                        ci.setCursorGetOpt("getInt");
                    }
                }
            }
        }

        return ci;
    }

    /**
     * 填充One注解的LinkInfo
     * @param typeElement
     * @param variableElement
     * @param entityInfo
     * @return
     */
    public LinkInfo generteOneLinkInfo(TypeElement typeElement , VariableElement variableElement, EntityInfo entityInfo ){
        LinkInfo linkInfo = new LinkInfo(msg);
        generteColInfo(entityInfo,linkInfo , typeElement , variableElement , null);
        One one = variableElement.getAnnotation(One.class);
        List<? extends AnnotationMirror> list = variableElement.getAnnotationMirrors();
        String f = one.field();
        EntityInfo entityTarget = null;
        for (AnnotationMirror annm:
             list) {
            if(annm.getAnnotationType().toString().equals(One.class.getName())){
                TypeMirror t = (TypeMirror) getAnnValue(annm , "target");
                TypeElement clazz = (TypeElement) ((DeclaredType)t).asElement();
                entityTarget = make(clazz);
                linkInfo.setTargetType(entityTarget);
            }
        }
        if(entityTarget == null){
            throw new RuntimeException();
        }
        linkInfo.setLinkType(LinkInfo.ONE);
        linkInfo.setHostField(linkInfo);
        linkInfo.setLinkedField(entityTarget.getField(f));
        return linkInfo;
    }

    /**
     * 填充Many注解的LinkInfo
     * @param typeElement
     * @param variableElement
     * @param entityInfo
     * @return
     */
    private LinkInfo generteManyLinkInfo(TypeElement typeElement, VariableElement variableElement, EntityInfo entityInfo ) {
        LinkInfo linkInfo = new LinkInfo(msg);
        generteColInfo(entityInfo,linkInfo , typeElement , variableElement , null);
        Many many = variableElement.getAnnotation(Many.class);
        List<? extends AnnotationMirror> list = variableElement.getAnnotationMirrors();
        String f = many.field();
        EntityInfo entityTarget = null;
        for (AnnotationMirror annm:
                list) {
            if(annm.getAnnotationType().toString().equals(Many.class.getName())){
                TypeMirror t = (TypeMirror) getAnnValue(annm , "target");
                TypeElement clazz = (TypeElement) ((DeclaredType)t).asElement();
                entityTarget = make(clazz);
                linkInfo.setTargetType(entityTarget);
            }
        }
        if(entityTarget == null || entityTarget.getPkType() == PkType.UNKNOWN || entityTarget.getPkType() == PkType.COMPOSITE){
            throw new RuntimeException();
        }
        linkInfo.setLinkType(LinkInfo.MANY);
        linkInfo.setHostField(linkInfo);
        linkInfo.setLinkedField(entityTarget.getField(f));
        return linkInfo;
    }

    private ExecutableElement getSeter(TypeElement  classElement , VariableElement var) {
        List<? extends Element> list = classElement.getEnclosedElements();
        String fn = Lang.upperFirst(var.getSimpleName());
        String setterName = "set" + fn;
        for (Element e:
                list) {
            if(ElementKind.METHOD == e.getKind()){
                if(e.getSimpleName().toString().equals(setterName)){
                    return (ExecutableElement) e;
                }
            }
        }
        return null;
    }

    private ExecutableElement getGeter(TypeElement  classElement,VariableElement var){
        List<? extends Element> list = classElement.getEnclosedElements();
        String fn = Lang.upperFirst(var.getSimpleName());
        String _get = "get" + fn;
        String _is = "is" + fn;
        for (Element e:
                list) {
            if(ElementKind.METHOD == e.getKind()){
                if(e.getSimpleName().toString().equals(_get) || e.getSimpleName().toString().equals(_is)){
                    return (ExecutableElement) e;
                }
            }
        }
        return null;
    }

    /**
     * 获取给定注解的值,主要用来获取Class类型的值,因为不能直接读取
     */
    private Object getAnnValue(AnnotationMirror ann , String method){

        Map<? extends ExecutableElement,? extends AnnotationValue> mapp = ann.getElementValues();
        for (ExecutableElement ee:
                mapp.keySet()) {
            if(ee.getSimpleName().toString().equals(method)){
                return mapp.get(ee).getValue();
            }
        }
        return null;
    }


    static class TableInfo {

        public Table annTable;

        public PK annPK;

    }
}
