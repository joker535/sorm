package com.guye.orm;

public interface SqlExpression {
    /**
     * 返回对应的sql语句。
     * @return 条件字符串
     */
    String toSql();

    /**
     * 返回sql预处理语句
     * 
     * @return
     */
    String toStatements();

    /**
     * 返回预处理参数
     * 
     * @return
     */
    String[] getArgs();
}
