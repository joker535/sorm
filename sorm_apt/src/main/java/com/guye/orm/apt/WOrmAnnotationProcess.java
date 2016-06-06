package com.guye.orm.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import javax.lang.model.util.Elements;

import com.guye.orm.annotation.ColAdapter;
import com.guye.orm.annotation.Column;
import com.guye.orm.annotation.ColumnAdapterFlag;
import com.guye.orm.annotation.Id;
import com.guye.orm.annotation.Many;
import com.guye.orm.annotation.Name;
import com.guye.orm.annotation.One;
import com.guye.orm.annotation.PK;
import com.guye.orm.annotation.Table;

import com.guye.orm.apt.type.EntityInfo;
import com.guye.orm.apt.utils.Constants;


/**
 * @author nieyu
 *
 */
public class WOrmAnnotationProcess extends AbstractProcessor {

	private Filer filer;
	private String logLevel;

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.RELEASE_7;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> set = new LinkedHashSet<>();
		set.add(Table.class.getCanonicalName() );
		set.add(Column.class.getCanonicalName());
		set.add(Id.class.getCanonicalName());
		set.add(Name.class.getCanonicalName());
		set.add(ColAdapter.class.getCanonicalName());
		set.add(ColumnAdapterFlag.class.getCanonicalName());
		set.add(PK.class.getCanonicalName());
		set.add(One.class.getCanonicalName());
		set.add(Many.class.getCanonicalName());
		return set;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		// 元素操作的辅助类
		filer = processingEnv.getFiler();
		Map<String, String> map =processingEnv.getOptions();
		for (String s : map.keySet()) {
		    if(s.equals("log")){
		        logLevel = map.get(s);
		    }
        }
		if(logLevel == null){
		    logLevel = "error";
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Messager messager = processingEnv.getMessager();
		messager.printMessage(Kind.NOTE, "start");

		Set<? extends Element> list = roundEnv.getElementsAnnotatedWith(ColumnAdapterFlag.class);
		for (Element ele : list) {
			if(ele instanceof  TypeElement){
				TypeElement typeElement = (TypeElement) ele;
//				messager.printMessage(Kind.NOTE,typeElement.getQualifiedName().toString());
				ColAdapterHolder.putAdapter(typeElement.getQualifiedName().toString() , typeElement);

			}
		}

		list = roundEnv.getElementsAnnotatedWith(Table.class);

		Map<String,String> genJavaClazz = new HashMap<String, String>();
		
		for (Element ele : list) {
			if (ele.getKind() == ElementKind.CLASS) {

				TypeElement classElement = (TypeElement) ele;
				OrmEntityInfoGenertor ormEntityInfoGenertor = new OrmEntityInfoGenertor(processingEnv,messager);
				
				EntityInfo entityInfo = ormEntityInfoGenertor.make(classElement);
				if(ele.getAnnotation(Table.class).genCode()){
				    genJavaClazz.put(classElement.getQualifiedName().toString(),EntityCodeGenertor.generteSrcFile(filer,entityInfo));
				}

			}
		}
		if(genJavaClazz.size() != 0){
		    messager.printMessage(Kind.NOTE, "end");
		    StringBuilder builder = new StringBuilder("package ").append(Constants.CORE_PKGNAME).append(";\nimport java.util.Map;\n").append(String.format("import %s.RecordAdapterFactory;\n" +
					"import %s.impl.RecordAdapter;\n" +
					"public class SOrmAdapterStaticCol implements RecordAdapterFactory{\n" +
					"private Map<Class, RecordAdapter> map = new java.util.concurrent.ConcurrentHashMap<Class, RecordAdapter>();\n{",Constants.CORE_PKGNAME,Constants.CORE_PKGNAME));
		    for (String string : genJavaClazz.keySet()) {
//		        messager.printMessage(Kind.NOTE, string);
		        builder.append(String.format("map.put(%s.class,new %s());",string , genJavaClazz.get(string)));
		    }
		    builder.append("}public RecordAdapter getRecordAdapter(Class clazz){return map.get(clazz);}}");
		    try {
		        JavaFileObject file = filer.createSourceFile(Constants.CORE_PKGNAME+".SOrmAdapterStaticCol", list.iterator().next());
		        Writer witrer = file.openWriter();
		        witrer.write(builder.toString());
		        witrer.flush();
		        witrer.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		        throw new RuntimeException(e);
		    }
		}
		return true;
	}

}
