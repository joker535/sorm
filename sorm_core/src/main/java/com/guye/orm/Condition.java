package com.guye.orm;

/**
 * @author nieyu
 *
 */
public interface Condition extends SqlExpression{

    SqlExpression where();
    SqlExpression groupby();
    SqlExpression orderby();

}