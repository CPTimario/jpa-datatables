package com.github.cptimario.datatables;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryParameterTest {
    private QueryParameter queryParameter;
    private QueryParameter emptyQueryParameter;

    @BeforeEach
    void setUp() {
        queryParameter = new QueryParameter();
        emptyQueryParameter = new QueryParameter();

        queryParameter.addWhereCondition("firstField = 1");
        queryParameter.addWhereCondition("secondField = 1");
        queryParameter.addGroupByField("firstField");
        queryParameter.addHavingCondition("count(firstField) >= 1");
        queryParameter.addHavingCondition("count(firstField) <= 1");
        queryParameter.addOrderCondition("firstField");
        queryParameter.addOrderCondition("secondField desc");
    }

    @Test
    void getWhereClauseTest() {
        String whereClause = " Where " + String.join(" And ", queryParameter.getWhereConditions());
        assertEquals(whereClause, queryParameter.getWhereClause());
        assertEquals("", emptyQueryParameter.getWhereClause());
    }

    @Test
    void getGroupByClauseTest() {
        String groupByClause = " Group By " + String.join(", ", queryParameter.getGroupByFields());
        assertEquals(groupByClause, queryParameter.getGroupByClause());
        assertEquals("", emptyQueryParameter.getGroupByClause());
    }

    @Test
    void getHavingClauseTest() {
        String havingClause = " Having " + String.join(" And ", queryParameter.getHavingConditions());
        assertEquals(havingClause, queryParameter.getHavingClause());
        assertEquals("", emptyQueryParameter.getHavingClause());
    }

    @Test
    void getOrderClauseTest() {
        String orderClause = " Order By " + String.join(", ", queryParameter.getOrderConditions());
        assertEquals(orderClause, queryParameter.getOrderByClause());
        assertEquals("", emptyQueryParameter.getOrderByClause());
    }
}
