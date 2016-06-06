package com.guye.orm;

import java.util.List;

import android.content.ContentValues;

/**
 * @author nieyu
 *
 */
public interface Dao {

    /**
     * 将一个对象插入到一个数据源。
    
     * @param obj
     *            要被插入的对象
     *            <p>
     *            它可以是：
     *            <ul>
     *            <li>普通
     *            <li>集合
     *            <li>数组
     *            </ul>
     *            <b style=color:red>注意：</b> 如果是集合，数组或者 Map，所有的对象必须类型相同，否则可能会出错
     * 
     * @return 插入后的对象
     * 
     */
    <T> int insert( T obj );
    <T> int insertWith( T obj , String regex);
    <T> int insertLink( T obj , String regex);

    /**
     * 更新一个对象。对象必须有 '@Id' 或者 '@Name' 或者 '@PK' 声明。
     * <p>
     * 并且调用这个函数前， 主键的值必须保证是有效，否则会更新失败
     * <p>
     * 这个对象所有的字段都会被更新，即，所有的没有被设值的字段，都会被置成 NULL，如果遇到 NOT NULL 约束，则会引发异常。
     * 如果想有选择的更新个别字段，请使用 org.nutz.dao.FieldFilter
     * <p>
     * @param obj
     *            要被更新的对象
     *            <p>
     *            它可以是：
     *            <ul>
     *            <li>普通
     *            <li>集合
     *            <li>数组
     *            </ul>
     *            <b style=color:red>注意：</b> 如果是集合，数组或者 Map，所有的对象必须类型相同，否则可能会出错
     * 
     * @return 返回实际被更新的记录条数，一般的情况下，如果更新成功，返回 1，否则，返回 -1
     * 
     */
    <T> int update( T obj );

    /**
     * 更新对象一部分字段
     * 
     * @param obj
     *            对象
     * @param regex
     *            正则表达式描述要被更新的字段
     * @return 返回实际被更新的记录条数，一般的情况下，如果更新成功，返回 1，否则，返回 -1
     */
    <T> int update( T obj, String regex );

    <T> int updateLink(T obj , String regex);
    <T> int updateWith(T obj , String regex);
    
    
    <T> int update(Class<T> classOfT , ContentValues values , Condition cnd);
    /**
     * 查询一组对象。你可以为这次查询设定条件，并且只获取一部分对象（翻页）
     * 
     * @param classOfT
     *            对象类型
     * @param cnd
     *            WHERE 条件。如果为 null，将获取全部数据，顺序为数据库原生顺序
     * @param pager
     *            翻页信息。如果为 null，则一次全部返回
     * @return 对象列表
     */
    <T> List<T> query( Class<T> classOfT, Condition cnd, Pager pager );

    /**
     * 查询一组对象。你可以为这次查询设定条件
     * 
     * @param classOfT
     *            对象类型
     * @param cnd
     *            WHERE 条件。如果为 null，将获取全部数据，顺序为数据库原生顺序<br>
     *            只有在调用这个函数的时候， cnd.limit 才会生效
     * @return 对象列表
     */
    <T> List<T> query( Class<T> classOfT, Condition cnd );

    <T> int queryLink(List<T> list , String regex);
    
    /**
     * 根据对象 ID 删除一个对象。它只会删除这个对象，关联对象不会被删除。
     * <p>
     * 你的对象必须在某个字段声明了注解 '@Id'，否则本操作会抛出一个运行时异常
     * <p>
     * 如果你设定了外键约束，没有正确的清除关联对象会导致这个操作失败
     * 
     * 
     * @param classOfT
     *            对象类型
     * @param id
     *            对象 ID
     * 
     * @return 影响的行数
     * @see org.nutz.dao.entity.annotation.Id
     * 
     */
    int delete( Class<?> classOfT, long id );

    /**
     * 根据对象 Name 删除一个对象。它只会删除这个对象，关联对象不会被删除。
     * <p>
     * 你的对象必须在某个字段声明了注解 '@Name'，否则本操作会抛出一个运行时异常
     * <p>
     * 如果你设定了外键约束，没有正确的清除关联对象会导致这个操作失败
     * 
     * @param classOfT
     *            对象类型
     * @param name
     *            对象 Name
     * 
     * @return 影响的行数
     * @see org.nutz.dao.entity.annotation.Name
     */
    int delete( Class<?> classOfT, String name );

    /**
     * 根据复合主键，删除一个对象。该对象必须声明 '@PK'，并且，给定的参数顺序 必须同 '@PK' 中声明的顺序一致，否则会产生不可预知的错误。
     * 
     * @param classOfT
     * @param pks
     *            复合主键需要的参数，必须同 '@PK'中声明的顺序一致
     */
    <T> int deletex( Class<T> classOfT, Object... pks );

    /**
     * 自动判断如何删除一个对象。
     * <p>
     * 如果声明了 '@Id' 则相当于 delete(Class<T>,long)<br>
     * 如果声明了 '@Name'，则相当于 delete(Class<T>,String)<br>
     * 如果声明了 '@PK'，则 deletex(Class<T>,Object ...)<br>
     * 如果没声明任何上面三个注解，则会抛出一个运行时异常
     * 
     * @param obj
     *            要被删除的对象
     */
    int delete( Object obj );
    int deleteLink( Object obj ,String regex);
    int deleteWith( Object obj ,String regex);

    /**
     * 根据一个 WHERE 条件，清除一组对象。只包括对象本身，不包括关联字段
     * 
     * @param classOfT
     *            对象类型
     * @param cnd
     *            查询条件，如果为 null，则全部清除
     * @return 影响的行数
     */
    int clear( Class<?> classOfT, Condition cnd );
    /**
     * 清除对象所有的记录
     * 
     * @param classOfT
     *            对象类型
     * @return 影响的行数
     */
    int clear( Class<?> classOfT );

    /**
     * * 根据条件，计算某个数据表或视图中有多少条记录
     * 
     * @param tableName
     *            表名
     * @param cnd
     *            WHERE 条件
     * @return 数量
     */
    int count( Class<?> classOfT, Condition cnd );

    /**
     * 计算某个数据表或视图中有多少条记录
     * 
     * @param tableName
     *            表名
     * @return 数量
     */
    int count( Class<?> classOfT );
    
    /**
     * 开始一个事务，事务中的dao操作使用方法返回的Dao对象
     * @return -1表示事务失败，其他值表示成功。注意：这个方法不会抛出异常。
     */
    int exeTransaction(Transaction transaction);
    
}
