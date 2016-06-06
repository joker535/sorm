package com.guye.orm.apt;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.TypeElement;

/**
 * Created by nieyu on 16/5/19.
 * 用来暂存ColumnAdapter实现类的TypeElement
 */
public class ColAdapterHolder {

    private static Map<String , TypeElement> sColAdapters = new HashMap<>();

    public static TypeElement getAdapter(String name){
        return sColAdapters.get(name);
    }

    public static void putAdapter(String str, TypeElement typeElement){
        sColAdapters.put(str, typeElement);
    }
}
