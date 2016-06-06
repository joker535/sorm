package com.guye.orm.apt.utils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Lang {



    /**
     * 获取一个 Type 类型实际对应的Class
     * 
     * @param type
     *            类型
     * @return 与Type类型实际对应的Class
     */
    @SuppressWarnings("rawtypes")
    public static Class<?> getTypeClass( Type type ) {
        Class<?> clazz = null;
        if (type instanceof Class<?>) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            clazz = (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> typeClass = getTypeClass(gat.getGenericComponentType());
            return Array.newInstance(typeClass, 0).getClass();
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;
            Type[] ts = tv.getBounds();
            if (ts != null && ts.length > 0)
                return getTypeClass(ts[0]);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] t_low = wt.getLowerBounds();// 取其下界
            if (t_low.length > 0)
                return getTypeClass(t_low[0]);
            Type[] t_up = wt.getUpperBounds(); // 没有下界?取其上界
            return getTypeClass(t_up[0]);// 最起码有Object作为上界
        }
        return clazz;
    }

    /**
     * 判断一个对象是否为空。它支持如下对象类型：
     * <ul>
     * <li>null : 一定为空
     * <li>数组
     * <li>集合
     * <li>Map
     * <li>其他对象 : 一定不为空
     * </ul>
     * 
     * @param obj
     *            任意对象
     * @return 是否为空
     */
    public static boolean isEmptyCol( Object obj ) {
        if (obj == null)
            return true;
        if (obj.getClass().isArray())
            return Array.getLength(obj) == 0;
        if (obj instanceof Collection<?>)
            return ((Collection<?>) obj).isEmpty();
        if (obj instanceof Map<?, ?>)
            return ((Map<?, ?>) obj).isEmpty();
        return false;
    }

    /**
     * 将一个字符串由驼峰式命名变成分割符分隔单词
     * 
     * <pre>
     *  lowerWord("helloWorld", '-') => "hello-world"
     * </pre>
     * 
     * @param cs
     *            字符串
     * @param c
     *            分隔符
     * 
     * @return 转换后字符串
     */
    public static String lowerWord( CharSequence cs, char c ) {
        StringBuilder sb = new StringBuilder();
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            char ch = cs.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0)
                    sb.append(c);
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 如果是数组或集合取得第一个对象。 否则返回自身
     * 
     * @param obj
     *            任意对象
     * @return 第一个代表对象
     */
    public static Object first( Object obj ) {
        if (null == obj)
            return obj;

        if (obj instanceof Collection<?>) {
            Iterator<?> it = ((Collection<?>) obj).iterator();
            return it.hasNext() ? it.next() : null;
        }

        if (obj.getClass().isArray())
            return Array.getLength(obj) > 0 ? Array.get(obj, 0) : null;

        return obj;
    }

    /**
     * 返回一个 Type 的泛型数组, 如果没有, 则直接返回null
     * 
     * @param type
     *            类型
     * @return 一个 Type 的泛型数组, 如果没有, 则直接返回null
     */
    public static Type[] getGenericsTypes( Type type ) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return pt.getActualTypeArguments();
        }
        return null;
    }

    /**
     * 将字符串首字母大写
     * 
     * @param s
     *            字符串
     * @return 首字母大写后的新字符串
     */
    public static String upperFirst( CharSequence s ) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == 0)
            return "";
        char c = s.charAt(0);
        if (Character.isUpperCase(c))
            return s.toString();
        return new StringBuilder(len).append(Character.toUpperCase(c))
                .append(s.subSequence(1, len)).toString();
    }

    /**
     * 将字符串首字母小写
     * 
     * @param s
     *            字符串
     * @return 首字母小写后的新字符串
     */
    public static String lowerFirst( CharSequence s ) {
        if (null == s)
            return null;
        int len = s.length();
        if (len == 0)
            return "";
        char c = s.charAt(0);
        if (Character.isLowerCase(c))
            return s.toString();
        return new StringBuilder(len).append(Character.toLowerCase(c))
                .append(s.subSequence(1, len)).toString();
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     * 
     * @param s
     *            字符串
     * @param regex
     *            正则式
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank( String s, String regex ) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isEmptyString(st))
                continue;
            list.add(st.trim());
        }
        return list.toArray(new String[list.size()]);
    }

    private static boolean isEmptyString(String st) {
        if(st==null || st.length() == 0){
            return true;
        }
        return false;
    }

    /**
     * 判断一个数组内是否包括某一个对象。 它的比较将通过 equals(Object,Object) 方法
     * 
     * @param array
     *            数组
     * @param ele
     *            对象
     * @return true 包含 false 不包含
     */
    public static <T> boolean contains( T[] array, T ele ) {
        if (null == array)
            return false;
        for (T e : array) {
            if (e.equals(ele))
                return true;
        }
        return false;
    }

    public static <T> T castTo( Object src, Class<T> toType ) {
        return cast(src, null == src ? null : src.getClass(), toType);
    }

    /**
     * 转换一个 POJO 从一个指定的类型到另外的类型
     * 
     * @param src
     *            源对象
     * @param fromType
     *            源对象类型
     * @param toType
     *            目标类型
     * @return 目标对象
     *             如果没有找到转换器，或者转换失败
     * @throws
     */
    @SuppressWarnings({ "unchecked" })
    public static <F, T> T cast( Object src, Class<F> fromType, Class<T> toType )
             {
        if (null == src) {
            // 原生数据的默认值
            if (toType.isPrimitive()) {
                if (toType == int.class)
                    return (T) Integer.valueOf(0);
                else if (toType == long.class)
                    return (T) Long.valueOf(0L);
                else if (toType == byte.class)
                    return (T) Byte.valueOf((byte) 0);
                else if (toType == short.class)
                    return (T) Short.valueOf((short) 0);
                else if (toType == float.class)
                    return (T) Float.valueOf(.0f);
                else if (toType == double.class)
                    return (T) Double.valueOf(.0);
                else if (toType == boolean.class)
                    return (T) Boolean.FALSE;
                else if (toType == char.class)
                    return (T) Character.valueOf(' ');
                throw new RuntimeException();
            }
            // 是对象，直接返回 null
            return null;
        }

        if (fromType == toType || toType == null || fromType == null)
            return (T) src;

        if (fromType.getName().equals(toType.getName()))
            return (T) src;
        if (toType.isAssignableFrom(fromType))
            return (T) src;

        if (toType.isArray()) {
            Class<?> componentType = toType.getComponentType();
            if (null != componentType && fromType != String.class
                    && componentType.isAssignableFrom(fromType)) {
                Object array = Array.newInstance(componentType, 1);
                Array.set(array, 0, src);
                return (T) array;
            }
            if (fromType.isArray() && componentType.isAssignableFrom(fromType.getComponentType())) {
                int length = Array.getLength(src);
                Object array = Array.newInstance(fromType.getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    Array.set(array, i, Array.get(fromType, i));
                }
                return (T) array;
            }
            if (src instanceof Collection) {
                Collection collection = (Collection) src;
                int length = collection.size();
                Object array = Array.newInstance(toType.getComponentType(), length);
                int i = 0;
                for (Object object : collection) {
                    Array.set(array, i++, object);
                }
                return (T) array;
            }
        }
        if (toType.isAssignableFrom(Collection.class)) {
            if (fromType.isArray()) {
                int length = Array.getLength(src);
                Collection collection;
                try {
                    collection = (Collection) toType.newInstance();
                    for (int i = 0; i < length; i++) {
                        collection.add(Array.get(fromType, i));
                    }
                    return (T) collection;
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("can not cast to:" + toType.getName());
                }
            }
            if (src instanceof Collection) {
                try {
                    Collection srcCollection = (Collection) src;
                    Collection collection = (Collection) toType.newInstance();
                    int length = collection.size();
                    int i = 0;
                    for (Object object : srcCollection) {
                       collection.add(object);
                    }
                    return (T) collection;
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("can not cast to:" + toType.getName());
                }
            }
        }
        throw new RuntimeException(fromType==null?"":fromType.getName() + "can not cast to:" + toType.getName());
    }

    public static Throwable unwrapThrow( Throwable e ) {
        if (e == null)
            return null;
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (itE.getTargetException() != null)
                return unwrapThrow(itE.getTargetException());
        }
        if (e instanceof RuntimeException && e.getCause() != null)
            return unwrapThrow(e.getCause());
        return e;
    }

    public static boolean isEmpty(String value) {
        if(value == null || value.length()==0){
            return  true;
        }
        return false;
    }
}
