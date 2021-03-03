package com.github.cptimario.datatables;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class QueryParameterTest {
    private QueryParameter queryParameter;
    private QueryParameter emptyQueryParameter;
    private LinkedHashSet<String> whereConditions;
    private LinkedHashSet<String> groupByFields;
    private LinkedHashSet<String> havingConditions;
    private LinkedHashSet<String> orderConditions;
    private String additionalCondition;

    @BeforeEach
    void setUp() {
        additionalCondition = "test condition";
        queryParameter = new QueryParameter();
        emptyQueryParameter = new QueryParameter();

        whereConditions = new LinkedHashSet<>();
        whereConditions.add("firstField = 1");
        whereConditions.add("secondField = 1");
        whereConditions.add("thirdField = 1");

        groupByFields = new LinkedHashSet<>();
        groupByFields.add("firstField");
        groupByFields.add("secondField");

        havingConditions = new LinkedHashSet<>();
        havingConditions.add("firstField = 2");
        havingConditions.add("secondField = 2");

        orderConditions = new LinkedHashSet<>();
        orderConditions.add("firstField");
        orderConditions.add("secondField");

        queryParameter.setWhereConditions(whereConditions);
        queryParameter.setGroupByFields(groupByFields);
        queryParameter.setHavingConditions(havingConditions);
        queryParameter.setOrderConditions(orderConditions);
    }

    @Test
    void getWhereConditionsTest() {
        assertIterableEquals(whereConditions, queryParameter.getWhereConditions());
        assertIterableEquals(Collections.emptySet(), emptyQueryParameter.getWhereConditions());
    }

    @Test
    void getGroupByFieldsTest() {
        assertIterableEquals(groupByFields, queryParameter.getGroupByFields());
        assertIterableEquals(Collections.emptySet(), emptyQueryParameter.getGroupByFields());
    }

    @Test
    void getHavingConditionsTest() {
        assertIterableEquals(havingConditions, queryParameter.getHavingConditions());
        assertIterableEquals(Collections.emptySet(), emptyQueryParameter.getHavingConditions());
    }

    @Test
    void getOrderConditionsTest() {
        assertIterableEquals(orderConditions, queryParameter.getOrderConditions());
        assertIterableEquals(Collections.emptySet(), emptyQueryParameter.getOrderConditions());
    }

    @Test
    void setWhereConditionsTestListInput() {
        List<String> whereConditionList = new ArrayList<>(whereConditions);
        emptyQueryParameter.setWhereConditions(whereConditionList);
        assertIterableEquals(whereConditions, emptyQueryParameter.getWhereConditions());
    }

    @Test
    void setGroupByFieldsTestListInput() {
        List<String> groupByFieldList = new ArrayList<>(groupByFields);
        emptyQueryParameter.setGroupByFields(groupByFieldList);
        assertIterableEquals(groupByFields, emptyQueryParameter.getGroupByFields());
    }

    @Test
    void setHavingConditionsTestListInput() {
        List<String> havingConditionList = new ArrayList<>(havingConditions);
        emptyQueryParameter.setHavingConditions(havingConditionList);
        assertIterableEquals(havingConditions, emptyQueryParameter.getHavingConditions());
    }

    @Test
    void setOrderConditionsTestListInput() {
        List<String> orderConditionList = new ArrayList<>(orderConditions);
        emptyQueryParameter.setOrderConditions(orderConditionList);
        assertIterableEquals(orderConditions, emptyQueryParameter.getOrderConditions());
    }

    @Test
    void addWhereConditionTest() {
        whereConditions.add(additionalCondition);
        queryParameter.addWhereCondition(additionalCondition);
        emptyQueryParameter.addWhereCondition(additionalCondition);
        assertIterableEquals(whereConditions, queryParameter.getWhereConditions());
        assertIterableEquals(List.of(additionalCondition), emptyQueryParameter.getWhereConditions());
    }

    @Test
    void addGroupByFieldTest() {
        groupByFields.add(additionalCondition);
        queryParameter.addGroupByField(additionalCondition);
        emptyQueryParameter.addGroupByField(additionalCondition);
        assertIterableEquals(groupByFields, queryParameter.getGroupByFields());
        assertIterableEquals(List.of(additionalCondition), emptyQueryParameter.getGroupByFields());
    }

    @Test
    void addHavingConditionTest() {
        havingConditions.add(additionalCondition);
        queryParameter.addHavingCondition(additionalCondition);
        emptyQueryParameter.addHavingCondition(additionalCondition);
        assertIterableEquals(havingConditions, queryParameter.getHavingConditions());
        assertIterableEquals(List.of(additionalCondition), emptyQueryParameter.getHavingConditions());
    }

    @Test
    void addOrderConditionTest() {
        orderConditions.add(additionalCondition);
        queryParameter.addOrderCondition(additionalCondition);
        emptyQueryParameter.addOrderCondition(additionalCondition);
        assertIterableEquals(orderConditions, queryParameter.getOrderConditions());
        assertIterableEquals(List.of(additionalCondition), emptyQueryParameter.getOrderConditions());
    }
}
