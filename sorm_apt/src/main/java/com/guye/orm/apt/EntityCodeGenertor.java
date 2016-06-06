package com.guye.orm.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import android.content.ContentValues;
import android.database.Cursor;
import com.guye.orm.Condition;
import com.guye.orm.apt.type.ColumnInfo;
import com.guye.orm.apt.type.EntityInfo;
import com.guye.orm.apt.type.LinkInfo;
import com.guye.orm.apt.type.PkType;
import com.guye.orm.apt.utils.Constants;
import com.guye.orm.impl.TemplateLinkField;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityCodeGenertor {
    public static String generteSrcFile( Filer filer, EntityInfo entityInfo ) {
        String pkgName = entityInfo.getBinName().toString();
        pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));

        TypeName typeName = TypeName.get(entityInfo.getTypeElement().asType());

        String objName = "obj" + entityInfo.getTypeElement().getSimpleName();
        MethodSpec.Builder methodBronObject = createBronObjectMethod(typeName, objName);
        MethodSpec.Builder methodGetObject = createGetObjectMethod(typeName, objName, entityInfo);

        MethodSpec.Builder methodSetObject = createSetObjectMethod(typeName, objName, entityInfo);

        MethodSpec.Builder methodGetPkType = createGetPkTypeMethod(entityInfo);

        MethodSpec.Builder methodGetTableName = MethodSpec.methodBuilder("getTableName")
                .returns(TypeName.get(String.class))
                .addParameter(Class.class, "classOf", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        methodGetTableName.addStatement("return $S", entityInfo.getTableName());

        MethodSpec.Builder methodGetPkName = createGetPkNamesMethod(entityInfo);

        MethodSpec.Builder methodGetPkNameAndValues = createGetPKNameAndValueMethod(typeName,
                objName, entityInfo);

        MethodSpec.Builder methodCreateSql = CreateGetCreateSqlMethod(entityInfo);

        TypeSpec.Builder build = TypeSpec.classBuilder(
                entityInfo.getBinName().replace(pkgName, "").substring(1) + "Adapter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        ClassName recordAdapterName = ClassName.get(Constants.IMPL_PKGNAME, "RecordAdapter");
        ClassName entityName = ClassName.get(pkgName, typeName.toString());
        build.superclass(ParameterizedTypeName.get(recordAdapterName, entityName));
        build.addMethod(methodBronObject.build());
        build.addMethod(methodGetObject.build());
        build.addMethod(methodSetObject.build());
        build.addMethod(methodCreateSql.build());
        build.addMethod(methodGetPkType.build());
        build.addMethod(methodGetPkName.build());
        build.addMethod(methodGetTableName.build());
        build.addMethod(methodGetPkNameAndValues.build());
        build.addMethod(createGetOneLinktMethod(typeName, objName, entityInfo).build());
        build.addMethod(createGetManyLinktMethod(typeName, objName, entityInfo).build());
        JavaFile javaFile = JavaFile.builder(pkgName, build.build()).build();

        try {
            JavaFileObject file = filer.createSourceFile(entityInfo.getBinName() + "Adapter",
                    entityInfo.getTypeElement());
            Writer witrer = file.openWriter();
            javaFile.writeTo(witrer);
            witrer.flush();
            witrer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return pkgName + "." + javaFile.typeSpec.name;

    }

    private static MethodSpec.Builder CreateGetCreateSqlMethod( EntityInfo entityInfo ) {
        MethodSpec.Builder methodCreateSql = MethodSpec.methodBuilder("getCreateSql")
                .addParameter(Class.class, "clazz", Modifier.FINAL)
                .returns(TypeName.get(String.class)).addModifiers(Modifier.PUBLIC);
        methodCreateSql.addStatement("StringBuilder builder = new StringBuilder()");
        int index = 0;
        for (ColumnInfo c : entityInfo.getColumnInfos()) {
            methodCreateSql.addCode(c.getCreateSqlCode("builder"));
            if (index != entityInfo.getColumnInfos().size() - 1) {
                methodCreateSql.addCode("$L.append(\",\");", "builder");
            }
            index++;
        }

        if (entityInfo.getSuperClass() != null
                && !entityInfo.getSuperClass().toString().equals("java.lang.Object")) {
            methodCreateSql.beginControlFlow("if(builder.length() >0)");
            methodCreateSql.addCode("builder.append(\",\");");
            methodCreateSql.endControlFlow();
            methodCreateSql
                    .addStatement(
                            "builder.append($L.DaoConfig.getConfig().getRecordAdapter($L.class).getCreateSql($L.class))",
                            Constants.CORE_PKGNAME, entityInfo.getSuperClass().toString(),
                            entityInfo.getSuperClass().toString());
        }

        methodCreateSql.addCode(CodeBlock.of("return builder.toString();"));
        return methodCreateSql;
    }

    private static MethodSpec.Builder createGetPKNameAndValueMethod( TypeName typeName,
            String objName, EntityInfo entityInfo ) {
        ClassName mapClassName = ClassName.get("java.util", "Map");
        ClassName stringClassName = ClassName.get("java.lang", "String");
        ClassName objClassName = ClassName.get("java.lang", "Object");
        MethodSpec.Builder methodGetPkNameAndValues = MethodSpec
                .methodBuilder("getPkNameAndValues")
                .returns(ParameterizedTypeName.get(mapClassName, stringClassName, objClassName))
                .addParameter(typeName, objName, Modifier.FINAL)
                .addParameter(Class.class, "classOf", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        if (entityInfo.getPkType() != null) {
            switch (entityInfo.getPkType()) {
            case ID:
                methodGetPkNameAndValues
                        .addStatement("Map<String, Object> map = new java.util.HashMap<String, Object>()");
                methodGetPkNameAndValues.addCode(entityInfo.getTheId().getContentValuesGetCode(
                        "map", objName));
                methodGetPkNameAndValues.addStatement("return map");
                break;
            case NAME:
                methodGetPkNameAndValues
                        .addStatement("Map<String, Object> map = new java.util.HashMap<String, Object>()");
                methodGetPkNameAndValues.addCode(entityInfo.getTheName().getContentValuesGetCode(
                        "map", objName));
                methodGetPkNameAndValues.addStatement("return map");
                break;
            case COMPOSITE:
                methodGetPkNameAndValues
                        .addStatement("Map<String, Object> map = new java.util.HashMap<String, Object>()");
                List<ColumnInfo> array = entityInfo.getTheComposites();
                for (int i = 0; i < array.size(); i++) {
                    methodGetPkNameAndValues.addCode(array.get(i).getContentValuesGetCode("map",
                            objName));
                }
                methodGetPkNameAndValues.addStatement("return map");
                break;

            default:
                methodGetPkNameAndValues.addStatement("return null");
                break;
            }
        } else {
            methodGetPkNameAndValues.addStatement("return null");
        }
        return methodGetPkNameAndValues;
    }

    private static MethodSpec.Builder createGetPkNamesMethod( EntityInfo entityInfo ) {
        MethodSpec.Builder methodGetPkName = MethodSpec.methodBuilder("getPkNames")
                .returns(TypeName.get(Object.class))
                .addParameter(Class.class, "classOf", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        if (entityInfo.getPkType() != null) {
            switch (entityInfo.getPkType()) {
            case ID:
                methodGetPkName.addStatement("return $S", entityInfo.getTheId().getColumnName());
                break;
            case NAME:
                methodGetPkName.addStatement("return $S", entityInfo.getTheName().getColumnName());
                break;
            case COMPOSITE:
                StringBuilder builder = new StringBuilder("return new String[]{");
                List<ColumnInfo> array = entityInfo.getTheComposites();
                String[] pkNames = new String[array.size()];
                for (int i = 0; i < array.size(); i++) {
                    pkNames[i] = array.get(i).getColumnName();
                    builder.append("$S");
                    if (i != array.size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append("}");
                
                methodGetPkName.addStatement(builder.toString(), (Object[])pkNames);
                break;

            default:
                methodGetPkName.addStatement("return null");
                break;
            }
        } else {
            methodGetPkName.addStatement("return null");
        }
        return methodGetPkName;
    }

    private static MethodSpec.Builder createGetPkTypeMethod( EntityInfo entityInfo ) {
        MethodSpec.Builder methodGetPkType = MethodSpec.methodBuilder("getPkType")
                .returns(TypeName.get(com.guye.orm.PkType.class))
                .addParameter(Class.class, "classOf", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        if (entityInfo.getPkType() != null) {
            methodGetPkType.addStatement("return $L.PkType.$L", Constants.CORE_PKGNAME,
                    entityInfo.getPkType().value);
        } else {
            methodGetPkType.addStatement("return $L.PkType.UNKNOWN", Constants.CORE_PKGNAME);
        }
        return methodGetPkType;
    }

    private static MethodSpec.Builder createSetObjectMethod( TypeName typeName, String objName,
            EntityInfo entityInfo ) {
        MethodSpec.Builder methodSetObject = MethodSpec.methodBuilder("createRecord")
                .returns(TypeName.get(ContentValues.class))
                .addParameter(typeName, objName, Modifier.FINAL)
                .addParameter(String.class, "regex", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        methodSetObject
                .addStatement("android.content.ContentValues contentValues = new android.content.ContentValues()");
        for (ColumnInfo c : entityInfo.getColumnInfos()) {
            try {
                MoreTypes.asPrimitiveType(c.getType());
                methodSetObject.beginControlFlow(
                        "if(android.text.TextUtils.isEmpty(regex) || $S.matches(regex))",
                        c.getName());
                methodSetObject.addCode(c.getContentValuesGetCode("contentValues", objName));
                methodSetObject.endControlFlow();
            } catch (IllegalArgumentException e) {
                methodSetObject
                        .beginControlFlow(
                                "if("
                                        + c.getGetFromObjectCode(objName).toString()
                                        + " != null && (android.text.TextUtils.isEmpty(regex) || $S.matches(regex)))",
                                c.getName());
                methodSetObject.addCode(c.getContentValuesGetCode("contentValues", objName));
                methodSetObject.endControlFlow();
            }

        }
        if (entityInfo.getSuperClass() != null
                && !entityInfo.getSuperClass().toString().equals("java.lang.Object")) {
            methodSetObject
                    .addStatement(
                            "contentValues.putAll($L.DaoConfig.getConfig().getRecordAdapter($L.class).createRecord($L,regex));",
                            Constants.CORE_PKGNAME, entityInfo.getSuperClass().toString(), objName);
        }
        methodSetObject.addCode(CodeBlock.of("return contentValues;"));
        return methodSetObject;
    }

    private static MethodSpec.Builder createBronObjectMethod( TypeName typeName, String objName ) {
        MethodSpec.Builder methodGetObject = MethodSpec.methodBuilder("bronObject")
                .returns(typeName).addParameter(Class.class, "clazz", Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC);
        methodGetObject.addCode("return new $L();", typeName);
        return methodGetObject;
    }

    private static MethodSpec.Builder createGetObjectMethod( TypeName typeName, String objName,
            EntityInfo entityInfo ) {
        MethodSpec.Builder methodGetObject = MethodSpec
                .methodBuilder("getObject")
                .returns(typeName)
                .addParameter(Cursor.class, "cs", Modifier.FINAL)
                .addParameter(Class.class, "clazz", Modifier.FINAL)
                .addParameter(TypeName.get(entityInfo.getTypeElement().asType()), objName,
                        Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        methodGetObject.addCode(CodeBlock.of("int index;"));
        for (ColumnInfo c : entityInfo.getColumnInfos()) {
            methodGetObject.addStatement("index=cs.getColumnIndex($S)", c.getColumnName());
            methodGetObject.beginControlFlow("if(!$L.isNull($L))", "cs", "index");
            methodGetObject.addCode(c.getCursorGetCode("cs", "index"));
            methodGetObject.addCode(c.getSet2ObjectCode(objName, c.getName()));
            methodGetObject.endControlFlow();
        }
        if (entityInfo.getSuperClass() != null
                && !entityInfo.getSuperClass().toString().equals("java.lang.Object")) {
            methodGetObject
                    .addStatement(
                            "$L.DaoConfig.getConfig().getRecordAdapter($L.class).getObject(cs,$L.class,$L)",
                            Constants.CORE_PKGNAME, entityInfo.getSuperClass().toString(),
                            entityInfo.getSuperClass().toString(), objName);
        }

        methodGetObject.addCode(CodeBlock.of("return $L;", objName));
        return methodGetObject;
    }

    private static MethodSpec.Builder createGetOneLinktMethod( TypeName typeName, String objName,
            EntityInfo entityInfo ) {
        ClassName listClassName = ClassName.get("java.util", "List");
        ClassName LinkedClassName = ClassName.get(Constants.CORE_PKGNAME, "LinkField");
        MethodSpec.Builder methodGetObject = MethodSpec.methodBuilder("getOneLinkFields")
                .returns(ParameterizedTypeName.get(listClassName, LinkedClassName))
                .addParameter(Class.class, "classOf", Modifier.FINAL)
                .addParameter(String.class, "regex", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        if (entityInfo.getOnes() == null || entityInfo.getOnes().size() == 0) {
            methodGetObject.addStatement("return java.util.Collections.EMPTY_LIST");
        } else {
            methodGetObject.addStatement(
                    "java.util.List<$L.LinkField> list = new java.util.ArrayList<$L.LinkField>()",
                    Constants.CORE_PKGNAME, Constants.CORE_PKGNAME);
            for (LinkInfo info : entityInfo.getOnes()) {
                TypeSpec.Builder comparator = TypeSpec.anonymousClassBuilder("").addSuperinterface(
                        TypeName.get(TemplateLinkField.class));

                MethodSpec.Builder builder = MethodSpec
                        .methodBuilder("setValue")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Object.class, "obj")
                        .addParameter(Object.class, "value")
                        .addCode(
                                info.getSet2ObjectCode(
                                        "(("
                                                + entityInfo.getTypeElement().getQualifiedName()
                                                        .toString() + ")obj)",
                                        "((" + info.getType().toString() + ")value)").toString());
                comparator.addMethod(builder.build());

                builder = MethodSpec
                        .methodBuilder("getValue")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Object.class, "obj")
                        .returns(Object.class)
                        .addStatement(
                                "return "
                                        + info.getGetFromObjectCode(
                                                "(("
                                                        + entityInfo.getTypeElement()
                                                                .getQualifiedName().toString()
                                                        + ")obj)").toString());
                comparator.addMethod(builder.build());

                ColumnInfo keyInfo = entityInfo.getPkType() == PkType.ID ? entityInfo.getTheId()
                        : entityInfo.getTheName();
                builder = MethodSpec
                        .methodBuilder("createCondition")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Object.class, "obj")
                        .returns(TypeName.get(Condition.class))
                        .addStatement(
                                "$L.utils.ConditionWraper cw = $L.utils.ConditionWraper.createConditionWraper()",
                                Constants.CORE_PKGNAME, Constants.CORE_PKGNAME)
                        .addStatement(
                                "cw.where().addEq($S, "
                                        + keyInfo.getGetFromObjectCode("(("
                                                + entityInfo.getTypeElement().getQualifiedName()
                                                        .toString() + ")obj)") + ")",
                                info.getLinkedField().getName()).addStatement("return cw");
                comparator.addMethod(builder.build());

                methodGetObject.addStatement("$L.impl.TemplateLinkField $LLink = $L",
                        Constants.CORE_PKGNAME, info.getName(), comparator.build());
                methodGetObject.addStatement("$LLink.clazz = $L.class", info.getName(), info
                        .getType().toString());
                methodGetObject.addStatement("$LLink.targetType = $L.class", info.getName(), info
                        .getTargetType().getTypeElement().getQualifiedName().toString());
                methodGetObject.addStatement("$LLink.type = $L", info.getName(),
                        Constants.CORE_PKGNAME + ".impl.TemplateLinkField.ONE");
                methodGetObject.addStatement(
                        "$LLink.ra = $L.DaoConfig.getConfig().getRecordAdapter($L.class)",
                        info.getName(), Constants.CORE_PKGNAME, info.getType().toString());

                methodGetObject.addStatement("list.add($LLink)", info.getName());
            }
            methodGetObject.addStatement("return list");
        }
        return methodGetObject;
    }

    private static MethodSpec.Builder createGetManyLinktMethod( TypeName typeName, String objName,
            EntityInfo entityInfo ) {
        ClassName listClassName = ClassName.get("java.util", "List");
        ClassName LinkedClassName = ClassName.get(Constants.CORE_PKGNAME, "LinkField");
        MethodSpec.Builder methodGetObject = MethodSpec.methodBuilder("getManyLinkFields")
                .returns(ParameterizedTypeName.get(listClassName, LinkedClassName))
                .addParameter(Class.class, "classOf", Modifier.FINAL)
                .addParameter(String.class, "regex", Modifier.FINAL).addModifiers(Modifier.PUBLIC);
        if (entityInfo.getManys() == null || entityInfo.getManys().size() == 0) {
            methodGetObject.addStatement("return java.util.Collections.EMPTY_LIST");
        } else {
            methodGetObject.addStatement(
                    "java.util.List<$L.LinkField> list = new java.util.ArrayList<$L.LinkField>()",
                    Constants.CORE_PKGNAME, Constants.CORE_PKGNAME);
            for (LinkInfo info : entityInfo.getManys()) {
                TypeSpec.Builder comparator = TypeSpec.anonymousClassBuilder("").addSuperinterface(
                        TypeName.get(TemplateLinkField.class));

                MethodSpec.Builder builder = MethodSpec.methodBuilder("setValue")
                        .addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
                        .addParameter(Object.class, "obj").addParameter(Object.class, "value");

                builder.beginControlFlow(
                        "if(!$L.class.toString().equals(value.getClass().getName()))",
                        removeParamType(info.getType().toString()));
                builder.addStatement("value = $L.utils.Lang.castTo(value, $L.class)",
                        Constants.CORE_PKGNAME, removeParamType(info.getType().toString()));
                builder.endControlFlow();
                builder.addCode(info.getSet2ObjectCode(
                        "((" + entityInfo.getTypeElement().getQualifiedName().toString() + ")obj)",
                        "((" + removeParamType(info.getType().toString()) + ")value)").toString());
                comparator.addMethod(builder.build());

                builder = MethodSpec
                        .methodBuilder("getValue")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Object.class, "obj")
                        .returns(Object.class)
                        .addStatement(
                                "return "
                                        + info.getGetFromObjectCode(
                                                "(("
                                                        + entityInfo.getTypeElement()
                                                                .getQualifiedName().toString()
                                                        + ")obj)").toString());
                comparator.addMethod(builder.build());

                ColumnInfo keyInfo = entityInfo.getPkType() == PkType.ID ? entityInfo.getTheId()
                        : entityInfo.getTheName();

                builder = MethodSpec
                        .methodBuilder("createCondition")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Object.class, "obj")
                        .returns(TypeName.get(Condition.class))
                        .addStatement(
                                "$L.utils.ConditionWraper cw = $L.utils.ConditionWraper.createConditionWraper()",
                                Constants.CORE_PKGNAME, Constants.CORE_PKGNAME)
                        .addStatement(
                                "cw.where().addEq($S, "
                                        + keyInfo.getGetFromObjectCode("(("
                                                + entityInfo.getTypeElement().getQualifiedName()
                                                        .toString() + ")obj)") + ")",
                                info.getLinkedField().getName()).addStatement("return cw");
                comparator.addMethod(builder.build());

                methodGetObject.addStatement("$L.impl.TemplateLinkField $LLink = $L",
                        Constants.CORE_PKGNAME, info.getName(), comparator.build());
                methodGetObject.addStatement("$LLink.clazz = $L.class", info.getName(),
                        removeParamType(info.getType().toString()));
                methodGetObject.addStatement("$LLink.targetType = $L.class", info.getName(), info
                        .getTargetType().getTypeElement().getQualifiedName().toString());
                methodGetObject.addStatement("$LLink.type = $L", info.getName(),
                        Constants.CORE_PKGNAME + ".impl.TemplateLinkField.ONE");
                methodGetObject.addStatement(
                        "$LLink.ra = $L.DaoConfig.getConfig().getRecordAdapter($L.class)",
                        info.getName(), Constants.CORE_PKGNAME, info.getTargetType()
                                .getTypeElement().getQualifiedName().toString());

                methodGetObject.addStatement("list.add($LLink)", info.getName());
            }
            methodGetObject.addStatement("return list");
        }
        return methodGetObject;
    }

    private static String removeParamType( String clzName ) {
        return clzName.replaceAll("<.*>", "");
    }
}
