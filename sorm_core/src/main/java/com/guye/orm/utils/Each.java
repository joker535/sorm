package com.guye.orm.utils;
/**
 * 回调接口
 * 
 * @param <T>
 */
public interface Each<T> {

    int EXIT = 1;
    int CONTINUE = 2;
    /**
     * 回调接口
     * 
     * @param index
     *            当前项目的下标
     * @param ele
     *            当前项目
     * @param length
     *            集合总长度，当然并不是所有的迭代器都会给出总长度的，-1 表示未知
     * @throws ExitLoop
     *             抛出这个异常，表示你打算退出循环
     * @throws ContinueLoop
     *             抛出这个异常，表示你打算停止递归，但是不会停止循环
     */
    int invoke(int index, T ele, int length);

}
