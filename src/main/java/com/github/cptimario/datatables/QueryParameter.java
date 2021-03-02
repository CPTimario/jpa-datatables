package com.github.cptimario.datatables;

import org.hibernate.Session;

import java.util.*;

public class QueryParameter extends HashMap<String, Object> {
    private Session session;
    private String selectClause;
    private Set<String> whereConditions;
    private Set<String> groupByFields;
    private Set<String> havingConditions;
    private Set<String> orderConditions;

    public QueryParameter() {
        this(null);
    }

    public QueryParameter(Session session) {
        this.session = session;
        this.selectClause = "";
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getSelectClause() {
        return selectClause;
    }

    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }

    public Set<String> getWhereConditions() {
        if (Objects.isNull(whereConditions))
            return Collections.emptySet();
        return whereConditions;
    }

    public void setWhereConditions(Set<String> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public void setWhereConditions(List<String> whereConditions) {
        this.whereConditions = new HashSet<>(whereConditions);
    }

    public Set<String> getGroupByFields() {
        if (Objects.isNull(groupByFields))
            return Collections.emptySet();
        return groupByFields;
    }

    public void setGroupByFields(Set<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public void setGroupByFields(List<String> groupByFields) {
        this.groupByFields = new HashSet<>(groupByFields);
    }

    public Set<String> getHavingConditions() {
        if (Objects.isNull(havingConditions))
            return Collections.emptySet();
        return havingConditions;
    }

    public void setHavingConditions(Set<String> havingConditions) {
        this.havingConditions = havingConditions;
    }

    public void setHavingConditions(List<String> havingConditions) {
        this.havingConditions = new HashSet<>(havingConditions);
    }

    public Set<String> getOrderConditions() {
        if (Objects.isNull(orderConditions))
            return Collections.emptySet();
        return orderConditions;
    }

    public void setOrderConditions(Set<String> orderConditions) {
        this.orderConditions = orderConditions;
    }

    public void setOrderConditions(List<String> orderConditions) {
        this.orderConditions = new HashSet<>(orderConditions);
    }

    public void addWhereCondition(String condition) {
        if (Objects.isNull(whereConditions))
            whereConditions = new LinkedHashSet<>();
        if (Objects.nonNull(condition) && !"".equals(condition.trim()))
            whereConditions.add(condition);
    }

    public void addGroupByField(String field) {
        if (Objects.isNull(groupByFields))
            groupByFields = new LinkedHashSet<>();
        if (Objects.nonNull(field) && !"".equals(field.trim()))
            groupByFields.add(field);
    }

    public void addHavingCondition(String condition) {
        if (Objects.isNull(havingConditions))
            havingConditions = new LinkedHashSet<>();
        if (Objects.nonNull(condition) && !"".equals(condition.trim()))
            havingConditions.add(condition);
    }

    public void addOrderCondition(String condition) {
        if (Objects.isNull(orderConditions))
            orderConditions = new LinkedHashSet<>();
        if (Objects.nonNull(condition) && !"".equals(condition.trim()))
            orderConditions.add(condition);
    }

    public String getWhereClause() {
        if (Objects.nonNull(whereConditions) && !whereConditions.isEmpty())
            return " Where " + String.join(" And ", whereConditions);
        return "";
    }

    public String getGroupByClause() {
        if (Objects.nonNull(groupByFields) && !groupByFields.isEmpty())
            return " Group By " + String.join(", ", groupByFields);
        return "";
    }

    public String getHavingClause() {
        if (Objects.nonNull(havingConditions) && !havingConditions.isEmpty())
            return " Having " + String.join(" And ", havingConditions);
        return "";
    }

    public String getOrderByClause() {
        if (Objects.nonNull(orderConditions) && !orderConditions.isEmpty())
            return " Order By " + String.join(", ", orderConditions);
        return "";
    }
}
