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

    public void setWhereConditions(LinkedHashSet<String> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public void setWhereConditions(List<String> whereConditions) {
        this.whereConditions = new LinkedHashSet<>(whereConditions);
    }

    public Set<String> getGroupByFields() {
        if (Objects.isNull(groupByFields))
            return Collections.emptySet();
        return groupByFields;
    }

    public void setGroupByFields(LinkedHashSet<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public void setGroupByFields(List<String> groupByFields) {
        this.groupByFields = new LinkedHashSet<>(groupByFields);
    }

    public Set<String> getHavingConditions() {
        if (Objects.isNull(havingConditions))
            return Collections.emptySet();
        return havingConditions;
    }

    public void setHavingConditions(LinkedHashSet<String> havingConditions) {
        this.havingConditions = havingConditions;
    }

    public void setHavingConditions(List<String> havingConditions) {
        this.havingConditions = new LinkedHashSet<>(havingConditions);
    }

    public Set<String> getOrderConditions() {
        if (Objects.isNull(orderConditions))
            return Collections.emptySet();
        return orderConditions;
    }

    public void setOrderConditions(LinkedHashSet<String> orderConditions) {
        this.orderConditions = orderConditions;
    }

    public void setOrderConditions(List<String> orderConditions) {
        this.orderConditions = new LinkedHashSet<>(orderConditions);
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
}
