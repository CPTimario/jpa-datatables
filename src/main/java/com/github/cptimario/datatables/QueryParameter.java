package com.github.cptimario.datatables;

import org.hibernate.Session;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class QueryParameter extends HashMap<String, String> {
    private Session session;
    private Set<String> whereCondition;
    private Set<String> groupByFields;
    private Set<String> orderConditions;

    public QueryParameter(Session session) {
        this.session = session;
        this.whereCondition = new LinkedHashSet<>();
        this.groupByFields = new LinkedHashSet<>();
        this.orderConditions = new LinkedHashSet<>();
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Set<String> getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(Set<String> whereCondition) {
        this.whereCondition = whereCondition;
    }

    public Set<String> getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(Set<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public Set<String> getOrderConditions() {
        return orderConditions;
    }

    public void setOrderConditions(Set<String> orderConditions) {
        this.orderConditions = orderConditions;
    }

    public void addWhereCondition(String condition) {
        whereCondition.add(condition);
    }

    public void addGroupByField(String field) {
        whereCondition.add(field);
    }

    public void addOrderCondition(String condition) {
        orderConditions.add(condition);
    }
}
