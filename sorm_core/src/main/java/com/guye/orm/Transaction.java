package com.guye.orm;

/**
 * @author nieyu
 * 封装一个事务。
 *
 */
public interface Transaction {
    /**
     * @param dao 事务中的Dao操作必须要用此dao对象。
     */
    void call(Dao dao);
}
