package com.guye.orm.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.guye.orm.DaoException;

/**
 * 包裹了 Class<?>， 提供了更多的反射方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * 
 * @param <T>
 */
public class Mirror<T> {

    /**
     * 包裹一个类
     * 
     * @param classOfT
     *            类
     * @return Mirror
     */
    public static <T> Mirror<T> me( Class<T> classOfT ) {
        return null == classOfT ? null : new Mirror<T>(classOfT);
    }

    /**
     * 生成一个对象的 Mirror
     * 
     * @param obj
     *            对象。
     * @return Mirror， 如果 对象 null，则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> Mirror<T> me( T obj ) {
        if (obj == null)
            return null;
        if (obj instanceof Class<?>)
            return (Mirror<T>) me((Class<?>) obj);
        return (Mirror<T>) me(obj.getClass());
    }

    /**
     * 根据Type生成Mirror, 如果type是 {@link ParameterizedType} 类型的对象<br>
     * 可以使用 getGenericsTypes() 方法取得它的泛型数组
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Mirror<T> me( Type type ) {
        if (null == type) {
            return null;
        }
        Mirror<T> mir = (Mirror<T>) Mirror.me(Lang.getTypeClass(type));
        mir.type = type;
        return mir;
    }

    private Class<T> klass;

    private Type     type;

    private Mirror(Class<T> classOfT) {
        klass = classOfT;
    }

    /**
     * 根据名称获取一个 Getter。
     * <p>
     * 比如，你想获取 abc 的 getter ，那么优先查找 getAbc()，如果没有则查找isAbc()，最后才是查找 abc()。
     * 
     * @param fieldName
     * @return 方法
     * @throws NoSuchMethodException
     *             没有找到 Getter
     */
    public Method getGetter( String fieldName ) throws NoSuchMethodException {
        return getGetter(fieldName, null);
    }

    /**
     * 根据名称和返回值获取一个 Getter。
     * <p>
     * 比如，你想获取 abc 的 getter ，那么优先查找 getAbc()，如果没有则查找isAbc()，最后才是查找 abc()。
     * 
     * @param fieldName
     *            字段名
     * @param returnType
     *            返回值
     * @return 方法
     * @throws NoSuchMethodException
     *             没有找到 Getter
     */
    public Method getGetter( String fieldName, Class<?> returnType ) throws NoSuchMethodException {
        String fn = Lang.upperFirst(fieldName);
        String _get = "get" + fn;
        String _is = "is" + fn;
        Method _m = null;
        for (Method method : klass.getMethods()) {
            if (method.getParameterTypes().length != 0)
                continue;

            Class<?> mrt = method.getReturnType();

            // 必须有返回类型
            if (null == mrt)
                continue;

            // 如果给了返回类型，用它判断一下
            if (null != returnType && !returnType.equals(mrt))
                continue;

            if (!method.isAccessible()) // 有些时候,即使是public的方法,也不一定能访问
                method.setAccessible(true);

            if (_get.equals(method.getName()))
                return method;

            if (_is.equals(method.getName())) {
                if (!Mirror.me(mrt).isBoolean())
                    throw new NoSuchMethodException();
                return method;
            }

            if (fieldName.equals(method.getName())) {
                _m = method;
                continue;
            }
        }
        if (_m != null)
            return _m;
        throw new NoSuchMethodException(String.format("Fail to find getter for [%s]->[%s]",
                klass.getName(), fieldName));
    }

    /**
     * 根据字段获取一个 Getter。
     * <p>
     * 比如，你想获取 abc 的 getter ，那么优先查找 getAbc()，如果 没有，则查找 abc()。
     * 
     * @param field
     * @return 方法
     * @throws NoSuchMethodException
     *             没有找到 Getter
     */
    public Method getGetter( Field field ) throws NoSuchMethodException {
        return getGetter(field.getName(), field.getType());
    }

    /**
     * 根据一个字段获取 Setter
     * <p>
     * 比如，你想获取 abc 的 setter ，那么优先查找 setAbc(T abc)，如果 没有，则查找 abc(T abc)。
     * 
     * @param field
     *            字段
     * @return 方法
     * @throws NoSuchMethodException
     *             没找到 Setter
     */
    public Method getSetter( Field field ) throws NoSuchMethodException {
        return getSetter(field.getName(), field.getType());
    }

    /**
     * 根据一个字段名和字段类型获取 Setter
     * 
     * @param fieldName
     *            字段名
     * @param paramType
     *            字段类型
     * @return 方法
     * @throws NoSuchMethodException
     *             没找到 Setter
     */
    public Method getSetter( String fieldName, Class<?> paramType ) throws NoSuchMethodException {
        try {
            String setterName = "set" + Lang.upperFirst(fieldName);
            try {
                return klass.getMethod(setterName, paramType);
            } catch (Throwable e) {
                try {
                    return klass.getMethod(fieldName, paramType);
                } catch (Throwable e1) {
                    Mirror<?> type = Mirror.me(paramType);
                    for (Method method : klass.getMethods()) {
                        if (method.getParameterTypes().length == 1)
                            if (method.getName().equals(setterName)
                                    || method.getName().equals(fieldName)) {
                                if (null == paramType
                                        || type.canCastToDirectly(method.getParameterTypes()[0]))
                                    return method;
                            }
                    }
                    // 还是没有? 会不会是包装类型啊?
                    if (!paramType.isPrimitive()) {
                        Class<?> p = unWrapper();
                        if (null != p)
                            return getSetter(fieldName, p);
                    }
                    throw new RuntimeException();
                }
            }
        } catch (Throwable e) {
            throw new NoSuchMethodException(String.format("Fail to find setter for [%s]->[%s(%s)]",
                    klass.getName(), fieldName, paramType.getName()));
        }
    }

    /**
     * 获取一个字段。这个字段可以是当前类型或者其父类的私有字段。
     * 
     * @param name
     *            字段名
     * @return 字段
     * @throws NoSuchFieldException
     */
    public Field getField( String name ) throws NoSuchFieldException {
        Class<?> cc = klass;
        while (null != cc && cc != Object.class) {
            try {
                return cc.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                cc = cc.getSuperclass();
            }
        }
        throw new NoSuchFieldException(String.format(
                "Can NOT find field [%s] in class [%s] and it's parents classes", name,
                klass.getName()));
    }

    /**
     * 获得当前类以及所有父类的所有的属性，包括私有属性。 <br>
     * 但是父类不包括 Object 类，并且，如果子类的属性如果与父类重名，将会将其覆盖
     * 
     * @return 属性列表
     */
    public Field[] getFields() {
        return _getFields(true, false, true, true);
    }

    private Field[] _getFields( boolean noStatic, boolean noMember, boolean noFinal, boolean noInner ) {
        Class<?> cc = klass;
        Map<String, Field> map = new LinkedHashMap<String, Field>();
        while (null != cc && cc != Object.class) {
            Field[] fs = cc.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                int m = f.getModifiers();
                if (noStatic && Modifier.isStatic(m))
                    continue;
                if (noFinal && Modifier.isFinal(m))
                    continue;
                if (noInner && f.getName().startsWith("this$"))
                    continue;
                if (noMember && !Modifier.isStatic(m))
                    continue;
                if (map.containsKey(fs[i].getName()))
                    continue;

                map.put(fs[i].getName(), fs[i]);
            }
            cc = cc.getSuperclass();
        }
        return map.values().toArray(new Field[map.size()]);
    }

    /**
     * 向父类递归查找某一个运行时注解
     * 
     * @param <A>
     *            注解类型参数
     * @param annType
     *            注解类型
     * @return 注解
     */
    public <A extends Annotation> A getAnnotation( Class<A> annType ) {
        Class<?> cc = klass;
        A ann;
        do {
            ann = cc.getAnnotation(annType);
            cc = cc.getSuperclass();
        } while (null == ann && cc != Object.class);
        return ann;
    }

    /**
     * 取得当前类型的泛型数组
     */
    public Type[] getGenericsTypes() {
        if (type instanceof ParameterizedType) {
            return Lang.getGenericsTypes(type);
        }
        return null;
    }

    /**
     * 取得当前类型的指定泛型
     */
    public Type getGenericsType( int index ) {
        Type[] ts = getGenericsTypes();
        return ts == null ? null : (ts.length <= index ? null : ts[index]);
    }

    /**
     * @return 所有静态方法
     */
    public Method[] getStaticMethods() {
        List<Method> list = new LinkedList<Method>();
        for (Method m : klass.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
                list.add(m);
        }
        return list.toArray(new Method[list.size()]);
    }

    /**
     * @return 对象类型
     */
    public Class<T> getType() {
        return klass;
    }

    /**
     * @return 本类型真实的类型（保留了范型信息）
     */
    public Type getActuallyType() {
        return type == null ? klass : type;
    }

    /**
     * @return 获得外覆类
     * 
     * @throws RuntimeException
     *             如果当前类型不是原生类型，则抛出
     */
    public Class<?> getWrapperClass() {
        if (!klass.isPrimitive()) {
            if (this.isPrimitiveNumber() || this.is(Boolean.class) || this.is(Character.class))
                return klass;
            throw new RuntimeException(String.format("Class '%s' should be a primitive class",
                    klass.getName()));
        }
        // TODO 用散列能快一点
        if (is(int.class))
            return Integer.class;
        if (is(char.class))
            return Character.class;
        if (is(boolean.class))
            return Boolean.class;
        if (is(long.class))
            return Long.class;
        if (is(float.class))
            return Float.class;
        if (is(byte.class))
            return Byte.class;
        if (is(short.class))
            return Short.class;
        if (is(double.class))
            return Double.class;

        throw new RuntimeException(String.format("Class [%s] has no wrapper class!",
                klass.getName()));
    }

    /**
     * @return 获得外覆类，如果没有外覆类，则返回自身的类型
     */
    public Class<?> getWrapper() {
        if (klass.isPrimitive())
            return getWrapperClass();
        return klass;
    }

    /**
     * @return 如果当前类为内部类，则返回其外部类。否则返回 null
     */
    public Class<?> getOuterClass() {
        if (Modifier.isStatic(klass.getModifiers()))
            return null;
        String name = klass.getName();
        int pos = name.lastIndexOf('$');
        if (pos == -1)
            return null;
        name = name.substring(0, pos);
        try {
            return getClass().getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 根据构造函数参数，创建一个对象。
     * 
     * @param args
     *            构造函数参数
     * @return 新对象
     * @throws DaoException
     */
    public T born() {
        T bc;
        bc = evalWithoutArgs();
        return bc;
    }

    public T evalWithoutArgs() {
        boolean isAbstract = Modifier.isAbstract(klass.getModifiers());

        // 先看看有没有默认构造函数
        try {
            if (!isAbstract) {
                Constructor<T> constructor = klass.getConstructor();
                return constructor.newInstance();
            }
        }
        // 如果没有默认构造函数 ...
        catch (Exception e) {
        }
        // 看看有没有带一个动态参数的构造函数
        try {
            if (!isAbstract) {
                for (Constructor<?> cons : klass.getConstructors()) {
                    Class<?>[] pts = cons.getParameterTypes();
                    if (pts.length == 1 && pts[0].isArray()) {
                        Object[] args = new Object[1];
                        args[0] = Mirror.blankArrayArg(pts);
                        return (T) cons.newInstance(args);
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new DaoException(e);
        }
        // 看看有没有默认静态工厂函数
        Method[] stMethods = getStaticMethods();
        try {
            for (Method m : stMethods) {
                if (m.getReturnType().equals(type) && m.getParameterTypes().length == 0) {
                    return (T) m.invoke(new Object[0]);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new DaoException(e);
        }
        // 看看有没有带一个动态参数的静态工厂函数
        try {
            for (Method m : stMethods) {
                Class<?>[] pts = m.getParameterTypes();
                if (m.getReturnType() == type && m.getParameterTypes().length == 1
                        && pts[0].isArray()) {
                    Object[] args = new Object[1];
                    args[0] = Mirror.blankArrayArg(pts);
                    return (T) m.invoke(args);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new DaoException(e);
        }

        return null;
    }

    /**
     * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
     * 
     * @param type
     *            类型
     * @return 是否相等
     */
    public boolean is( Class<?> type ) {
        return null != type && klass == type;
    }

    /**
     * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
     * 
     * @param className
     *            类型名称
     * @return 是否相等
     */
    public boolean is( String className ) {
        return klass.getName().equals(className);
    }

    /**
     * @param type
     *            类型或接口名
     * @return 当前对象是否为一个类型的子类，或者一个接口的实现类
     */
    public boolean isOf( Class<?> type ) {
        return type.isAssignableFrom(klass);
    }

    /**
     * @return 当前对象是否为字符串
     */
    public boolean isString() {
        return is(String.class);
    }

    /**
     * @return 当前对象是否为CharSequence的子类
     */
    public boolean isStringLike() {
        return CharSequence.class.isAssignableFrom(klass);
    }

    /**
     * @return 当前对象是否为字符
     */
    public boolean isChar() {
        return is(char.class) || is(Character.class);
    }

    /**
     * @return 当前对象是否为枚举
     */
    public boolean isEnum() {
        return klass.isEnum();
    }

    /**
     * @return 当前对象是否为布尔
     */
    public boolean isBoolean() {
        return is(boolean.class) || is(Boolean.class);
    }

    /**
     * @return 当前对象是否为浮点
     */
    public boolean isFloat() {
        return is(float.class) || is(Float.class);
    }

    /**
     * @return 当前对象是否为双精度浮点
     */
    public boolean isDouble() {
        return is(double.class) || is(Double.class);
    }

    /**
     * @return 当前对象是否为整型
     */
    public boolean isInt() {
        return is(int.class) || is(Integer.class);
    }

    /**
     * @return 当前对象是否为整数（包括 int, long, short, byte）
     */
    public boolean isIntLike() {
        return isInt() || isLong() || isShort() || isByte() || is(BigDecimal.class);
    }

    /**
     * @return 当前类型是不是接口
     */
    public boolean isInterface() {
        return klass.isInterface();
    }

    /**
     * @return 当前对象是否为小数 (float, dobule)
     */
    public boolean isDecimal() {
        return isFloat() || isDouble();
    }

    /**
     * @return 当前对象是否为长整型
     */
    public boolean isLong() {
        return is(long.class) || is(Long.class);
    }

    /**
     * @return 当前对象是否为短整型
     */
    public boolean isShort() {
        return is(short.class) || is(Short.class);
    }

    /**
     * @return 当前对象是否为字节型
     */
    public boolean isByte() {
        return is(byte.class) || is(Byte.class);
    }

    /**
     * @param type
     *            类型
     * @return 否为一个对象的外覆类
     */
    public boolean isWrapperOf( Class<?> type ) {
        try {
            return Mirror.me(type).getWrapperClass() == klass;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * @param type
     *            目标类型
     * @return 判断当前对象是否能直接转换到目标类型，而不产生异常
     */
    public boolean canCastToDirectly( Class<?> type ) {
        if (klass == type || type.isAssignableFrom(klass))
            return true;
        if (klass.isPrimitive() && type.isPrimitive()) {
            if (this.isPrimitiveNumber() && Mirror.me(type).isPrimitiveNumber())
                return true;
        }
        try {
            return Mirror.me(type).getWrapperClass() == this.getWrapperClass();
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * @return 当前对象是否为原生的数字类型 （即不包括 boolean 和 char）
     */
    public boolean isPrimitiveNumber() {
        return isInt() || isLong() || isFloat() || isDouble() || isByte() || isShort();
    }

    /**
     * 如果不是容器，也不是 POJO，那么它必然是个 Obj
     * 
     * @return true or false
     */
    public boolean isObj() {
        return isContainer() || isPojo();
    }

    /**
     * 判断当前类型是否为POJO。 除了下面的类型，其他均为 POJO
     * <ul>
     * <li>原生以及所有包裹类
     * <li>类字符串
     * <li>类日期
     * <li>非容器
     * </ul>
     * 
     * @return true or false
     */
    public boolean isPojo() {
        if (this.klass.isPrimitive() || this.isEnum())
            return false;

        if (this.isStringLike() || this.isDateTimeLike())
            return false;

        if (this.isPrimitiveNumber() || this.isBoolean() || this.isChar())
            return false;

        return !isContainer();
    }

    /**
     * 判断当前类型是否为容器，包括 Map，Collection, 以及数组
     * 
     * @return true of false
     */
    public boolean isContainer() {
        return isColl() || isMap();
    }

    /**
     * 判断当前类型是否为数组
     * 
     * @return true of false
     */
    public boolean isArray() {
        return klass.isArray();
    }

    /**
     * 判断当前类型是否为 Collection
     * 
     * @return true of false
     */
    public boolean isCollection() {
        return isOf(Collection.class);
    }

    /**
     * @return 当前类型是否是数组或者集合
     */
    public boolean isColl() {
        return isArray() || isCollection();
    }

    /**
     * 判断当前类型是否为 Map
     * 
     * @return true of false
     */
    public boolean isMap() {
        return isOf(Map.class);
    }

    /**
     * @return 当前对象是否在表示日期或时间
     */
    public boolean isDateTimeLike() {
        return Calendar.class.isAssignableFrom(klass)
                || java.util.Date.class.isAssignableFrom(klass);
    }

    /**
     * 根据函数参数类型数组的最后一个类型（一定是数组，表示变参），为最后一个变参生成一个空数组
     * 
     * @param pts
     *            函数参数类型列表
     * @return 变参空数组
     */
    public static Object blankArrayArg( Class<?>[] pts ) {
        return Array.newInstance(pts[pts.length - 1].getComponentType(), 0);
    }

    /**
     * 获取一个类的泛型参数数组，如果这个类没有泛型参数，返回 null
     */
    public static Type[] getTypeParams( Class<?> klass ) {
        // TODO 这个实现会导致泛型丢失,只能取得申明类型
        if (klass == null || "java.lang.Object".equals(klass.getName()))
            return null;
        // 看看父类
        Type superclass = klass.getGenericSuperclass();
        if (null != superclass && superclass instanceof ParameterizedType)
            return ((ParameterizedType) superclass).getActualTypeArguments();

        // 看看接口
        Type[] interfaces = klass.getGenericInterfaces();
        for (Type inf : interfaces) {
            if (inf instanceof ParameterizedType) {
                return ((ParameterizedType) inf).getActualTypeArguments();
            }
        }
        return getTypeParams(klass.getSuperclass());
    }

    private static final Pattern PTN = Pattern.compile("(<)(.+)(>)");

    /**
     * 获取一个字段的泛型参数数组，如果这个字段没有泛型，返回空数组
     * 
     * @param f
     *            字段
     * @return 泛型参数数组
     */
    public static Class<?>[] getGenericTypes( Field f ) {
        String gts = f.toGenericString();
        Matcher m = PTN.matcher(gts);
        if (m.find()) {
            String s = m.group(2);
            String[] ss = Lang.splitIgnoreBlank(s, ",");
            if (ss.length > 0) {
                Class<?>[] re = new Class<?>[ss.length];
                try {
                    for (int i = 0; i < ss.length; i++) {
                        String className = ss[i];
                        if (className.length() > 0 && className.charAt(0) == '?')
                            re[i] = Object.class;
                        else {
                            int pos = className.indexOf('<');
                            if (pos < 0)
                                re[i] = Class.forName(className);
                            else
                                re[i] = Class.forName(className.substring(0, pos));
                        }
                    }
                    return re;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException();
                }
            }
        }
        return new Class<?>[0];
    }

    /**
     * 获取一个字段的某一个泛型参数，如果没有，返回 null
     * 
     * @param f
     *            字段
     * @return 泛型参数数
     */
    public static Class<?> getGenericTypes( Field f, int index ) {
        Class<?>[] types = getGenericTypes(f);
        if (null == types || types.length <= index)
            return null;
        return types[index];
    }

    /**
     * 获取一个类的某个一个泛型参数
     * 
     * @param klass
     *            类
     * @param index
     *            参数下标 （从 0 开始）
     * @return 泛型参数类型
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getTypeParam( Class<?> klass, int index ) {
        Type[] types = getTypeParams(klass);
        if (index >= 0 && index < types.length) {
            Type t = types[index];
            Class<T> clazz = (Class<T>) Lang.getTypeClass(t);
            if (clazz == null)
                throw new RuntimeException(String.format("Type '%s' is not a Class", t.toString()));
            return clazz;
        }
        throw new RuntimeException(String.format("Class type param out of range %d/%d", index,
                types.length));
    }

    /**
     * @param klass
     *            类型
     * @return 一个类型的包路径
     */
    public static String getPath( Class<?> klass ) {
        return klass.getName().replace('.', '/');
    }

    /**
     * @param parameterTypes
     *            函数的参数类型数组
     * @return 参数的描述符
     */
    public static String getParamDescriptor( Class<?>[] parameterTypes ) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Class<?> pt : parameterTypes)
            sb.append(getTypeDescriptor(pt));
        sb.append(')');
        return sb.toString();
    }

    /**
     * @param klass
     *            类型
     * @return 获得一个类型的描述符
     */
    public static String getTypeDescriptor( Class<?> klass ) {
        if (klass.isPrimitive()) {
            if (klass == void.class)
                return "V";
            else if (klass == int.class)
                return "I";
            else if (klass == long.class)
                return "J";
            else if (klass == byte.class)
                return "B";
            else if (klass == short.class)
                return "S";
            else if (klass == float.class)
                return "F";
            else if (klass == double.class)
                return "D";
            else if (klass == char.class)
                return "C";
            else
                /* if(klass == boolean.class) */
                return "Z";
        }
        StringBuilder sb = new StringBuilder();
        if (klass.isArray()) {
            return sb.append('[').append(getTypeDescriptor(klass.getComponentType())).toString();
        }
        return sb.append('L').append(Mirror.getPath(klass)).append(';').toString();
    }

    public Class<?> unWrapper() {
        return TypeMapping2.get(klass);
    }

    private static final Map<Class<?>, Class<?>> TypeMapping2 = new HashMap<Class<?>, Class<?>>();

    static {

        TypeMapping2.put(Short.class, short.class);
        TypeMapping2.put(Integer.class, int.class);
        TypeMapping2.put(Long.class, long.class);
        TypeMapping2.put(Double.class, double.class);
        TypeMapping2.put(Float.class, float.class);
        TypeMapping2.put(Byte.class, byte.class);
        TypeMapping2.put(Character.class, char.class);
        TypeMapping2.put(Boolean.class, boolean.class);
    }
}
