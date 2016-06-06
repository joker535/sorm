package com.guye.orm.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.guye.orm.Condition;
import com.guye.orm.SqlExpression;

import android.text.TextUtils;

/**
 * ConditionBuidler
 * @author nieyu
 *
 */
public class ConditionWraper implements Condition{
    private static enum SqlExpType {
        name, // 只有名字的表达式，比如groupby子句
        relation, // 条件子句，where后面的
        logic // 逻辑子句，and or。。。
    }

    public static enum SqlLogic {
        and(" AND "), or(" OR ");
        public String name;

        private SqlLogic(String name) {
            this.name = name;
        }
    }

    public static class SqlExp implements SqlExpression {
        protected String   op;
        protected Object   value;
        protected Object   value2;
        protected boolean  not;
        protected String   name;
        private SqlExpType expType;

        @Override
        public String toSql() {
            return toSql(false);
        }
        
        private String toSql(boolean isStatements){
        	if(expType == null){
        		return "";
        	}
            if (expType == null && TextUtils.isEmpty(name)) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            switch (expType) {
            case name:
                if (TextUtils.isEmpty(op)) {
                    return name;
                } else {
                    return builder.append(name).append(" ").append(op).append(" ").toString();
                }
            case relation:
                if (TextUtils.isEmpty(op)) {
                    return "";
                }
                switch (op) {
                case "<":
                case ">":
                case ">=":
                case "<=":
                case "=":
                case "<>":
                    if (not) {
                        throw new RuntimeException("not support 'not' option");
                    }
                    builder.append(name).append(op).append(getString(isStatements))
                            .append(" ");
                    break;
                case "IS":
                    builder.append(name).append(" ").append(op);
                    if (not) {
                        builder.append("NOT ");
                    }
                    builder.append("NULL ");
                    break;
                case "IN":
                case "BETWEEN":
                case "LIKE":
                    builder.append(name).append(" ");
                    if (not) {
                        builder.append("NOT ");
                    }
                    builder.append(op).append(" ").append(getString(isStatements));
                    break;

                default:
                    break;
                }
                break;
            case logic:
                return op;
            default:
                break;
            }
            return builder.toString();
        }

        private String getString(boolean isStatements) {
            StringBuilder builder = new StringBuilder();
            Mirror<?> mirror = Mirror.me(value.getClass());
            if (mirror.isArray()) {
                if (!op.equals("IN")) {
                    throw new RuntimeException("option :" + op + " not support array value");
                }
                Object[] objects = (Object[]) value;
                builder.append('(');
                for (Object object : objects) {
                    if(isStatements){
                        builder.append('?');
                    }else{
                        builder.append(v2Str(object , !isStatements));
                    }
                    builder.append(',');
                }
                builder.setCharAt(builder.length() - 1, ')');
            } else {
                builder.append(isStatements?'?':v2Str(value, !isStatements)).append(" ");
                if (op.endsWith("BETWEEN")) {
                    if (value2 == null) {
                        throw new RuntimeException("option :" + op + " not support singler value");
                    }
                    if (!mirror.is(value2.getClass())) {
                        throw new RuntimeException("option :" + op
                                + " not support two different value");
                    }
                    builder.append(" AND ").append(isStatements?'?':v2Str(value2, !isStatements)).append(" ");
                }
            }
            return builder.toString();
        }

        private String v2Str( Object object , boolean needQuote) {
            StringBuilder builder = new StringBuilder();
            Mirror<?> mirror = Mirror.me(object.getClass());
            if (mirror.isOf(CharSequence.class)) {
                if(needQuote){
                    builder.append('\'');
                }
                builder.append(((CharSequence) object).toString());
                if(needQuote){
                    builder.append('\'');
                }
            } else if (mirror.isPrimitiveNumber()) {
                builder.append(String.valueOf(object));
            } else if (mirror.isBoolean()) {
                if(needQuote){
                    builder.append('\'');
                }
                builder.append(String.valueOf(object));
                if(needQuote){
                    builder.append('\'');
                }
            }
            return builder.toString();

        }

        @Override
        public String toStatements() {
            return toSql(true);
        }

        @Override
        public String[] getArgs() {
            if(value == null){
                return new String[0];
            }
            Mirror<?> mirror = Mirror.me(value.getClass());
            if (mirror.isArray()) {
                if (!op.equals("IN")) {
                    throw new RuntimeException("option :" + op + " not support array value");
                }
                Object[] objects = (Object[]) value;
                String[] result = new String[objects.length];
                for (int i = 0; i < result.length; i++) {
                    result[i] = (v2Str(objects[i], false));
                }
                return result;
            } else {
                String[] result ;
                if(value2 == null){
                    result = new String[1];
                    result[0] = v2Str(value, false);
                }else{
                    result = new String[2];
                    result[0] = v2Str(value, false);
                    result[1] = v2Str(value2, false);
                }
                return result;
            }
        }
    }

    public static class Where implements SqlExpression {
        private List<SqlExpression> conditions = new ArrayList<SqlExpression>();

        public Where addNull( String col ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            conditions.add(expression);
            return this;
        }

        public Where addNotNull( String col ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.not = true;
            conditions.add(expression);
            return this;
        }

        public Where addEq( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "=";
            expression.value = object;
            conditions.add(expression);
            return this;
        }
        
        public Where addLike( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "LIKE";
            expression.value = object;
            conditions.add(expression);
            return this;
        }

        public Where addNotEq( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "<>";
            expression.value = object;
            conditions.add(expression);
            return this;
        }

        public Where addIn( String col, Object[] objects ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "IN";
            expression.value = objects;
            conditions.add(expression);
            return this;
        }

        public Where addNotIn( String col, Object[] objects ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "IN";
            expression.value = objects;
            expression.not = true;
            conditions.add(expression);
            return this;
        }

        public Where addLT( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "<";
            expression.value = object;
            conditions.add(expression);
            return this;
        }

        public Where addGT( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = ">";
            expression.value = object;
            conditions.add(expression);
            return this;
        }

        public Where addGTE( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = ">=";
            expression.value = object;
            conditions.add(expression);
            return this;
        }

        public Where addlTE( String col, Object object ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "<=";
            expression.value = object;
            conditions.add(expression);
            return this;
        }

        public Where addBetween( String col, Object o1, Object o2 ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "BETWEEN";
            expression.value = o1;
            expression.value2 = o2;
            conditions.add(expression);
            return this;
        }

        public Where addNotBetween( String col, Object o1, Object o2 ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.relation;
            expression.name = col;
            expression.op = "BETWEEN";
            expression.value = o1;
            expression.value2 = o2;
            expression.not = true;
            conditions.add(expression);
            return this;
        }

        public Where addLogic( SqlLogic logic ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.logic;
            expression.op = logic.name;
            conditions.add(expression);
            return this;
        }

        public Where addCondition( Condition condition ) {
            conditions.add(condition);
            return this;
        }

        @Override
        public String toSql() {
            if(conditions.size()==0){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            for (SqlExpression condition : conditions) {
                builder.append(condition.toSql());
            }
            builder.append(')');
            return builder.toString();
        }

        @Override
        public String toStatements() {
            if(conditions.size()==0){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            for (SqlExpression condition : conditions) {
                builder.append(condition.toStatements());
            }
            builder.append(')');
            return builder.toString();
        }

        @Override
        public String[] getArgs() {
            ArrayList<String> l = new ArrayList<String>(conditions.size());
            for (SqlExpression c : conditions) {
                Collections.addAll(l, c.getArgs());
            }
            return l.toArray(new String[0]);
        }
        
    }

    public static class Groupby implements SqlExpression {
        private Where   where;
        private List<SqlExpression> conditions = new ArrayList<SqlExpression>();

        public Groupby addGroupby( String col ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.name;
            expression.name = col;
            conditions.add(expression);
            return this;
        }

        public Where having() {
            where = new Where();
            return where;
        }

        @Override
        public String toSql() {
            if(conditions.size()==0){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (SqlExpression condition : conditions) {
                builder.append(condition.toSql());
            }
            return builder.toString();
        }

        @Override
        public String toStatements() {
            if(conditions.size()==0){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (SqlExpression condition : conditions) {
                builder.append(condition.toStatements());
            }
            return builder.toString();
        }

        @Override
        public String[] getArgs() {
            ArrayList<String> l = new ArrayList<String>(conditions.size());
            for (SqlExpression c : conditions) {
                Collections.addAll(l, c.getArgs());
            }
            return l.toArray(new String[0]);
        }
    }

    public static class OrderBy implements SqlExpression {
        private List<SqlExpression> conditions = new ArrayList<SqlExpression>();

        public OrderBy addOrderBy( String col, boolean isDesc ) {
            SqlExp expression = new SqlExp();
            expression.expType = SqlExpType.name;
            expression.name = col;
            expression.op = isDesc ? "DESC" : "ASC";
            conditions.add(expression);
            return this;
        }

        @Override
        public String toSql() {
            if(conditions.size()==0){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for (SqlExpression condition : conditions) {
                builder.append(condition.toSql());
            }
            return builder.toString();
        }

        @Override
        public String toStatements() {
            return toSql();
        }

        @Override
        public String[] getArgs() {
            ArrayList<String> l = new ArrayList<String>(conditions.size());
            for (SqlExpression c : conditions) {
                Collections.addAll(l, c.getArgs());
            }
            return l.toArray(new String[0]);
        }
    }

    private Where   where;
    private Groupby groupby;
    private OrderBy orderby;

    private ConditionWraper() {

    }

    public Where newWhere() {
        return new Where();
    }

    public Where where() {
        if (where == null) {
            where = new Where();
        }
        return where;
    }

    public Groupby groupby() {
        if (groupby == null) {
            groupby = new Groupby();
        }
        return groupby;
    }

    public OrderBy orderby() {
        if (orderby == null) {
            orderby = new OrderBy();
        }
        return orderby;
    }

    private String toSql(boolean isStatements ) {
        StringBuilder builder = new StringBuilder();
        if (where != null) {
            builder.append("WHERE ").append(isStatements?where.toStatements():where.toSql());
        }
        builder.append(" ");
        if (groupby != null) {
            builder.append("GROUP BY ").append(isStatements?groupby.toStatements():groupby.toSql());
        }
        builder.append(" ");
        if (orderby != null) {
            builder.append("ORDER BY ").append(isStatements?orderby.toStatements():orderby.toSql());
        }
        return builder.toString();
    }
    
    public static ConditionWraper createConditionWraper() {
        return new ConditionWraper();
    }

    @Override
    public String toSql() {
        return toSql(false);
    }
    
    @Override
    public String toStatements() {
        return toSql(true);
    }

    @Override
    public String[] getArgs() {
        return null;
    }

    public static ConditionWraper wrap( Condition cnd ) {
        if(cnd == null){
            return createConditionWraper();
        }
        return (ConditionWraper)cnd;
    }

}
