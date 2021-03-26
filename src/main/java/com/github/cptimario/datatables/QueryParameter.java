package com.github.cptimario.datatables;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryParameter extends HashMap<String, Object> {
    private String selectClause;
    private LinkedHashSet<String> whereConditions;
    private LinkedHashSet<String> groupByFields;
    private LinkedHashSet<String> havingConditions;
    private LinkedHashSet<String> orderConditions;

    public QueryParameter() {
       setSelectClause("");
       setWhereConditions(new LinkedHashSet<>());
       setGroupByFields(new LinkedHashSet<>());
       setHavingConditions(new LinkedHashSet<>());
       setOrderConditions(new LinkedHashSet<>());
    }

    public void setWhereConditions(LinkedHashSet<String> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public void setWhereConditions(List<String> whereConditions) {
        this.whereConditions = new LinkedHashSet<>(whereConditions);
    }

    public void setGroupByFields(LinkedHashSet<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public void setGroupByFields(List<String> groupByFields) {
        this.groupByFields = new LinkedHashSet<>(groupByFields);
    }

    public void setHavingConditions(LinkedHashSet<String> havingConditions) {
        this.havingConditions = havingConditions;
    }

    public void setHavingConditions(List<String> havingConditions) {
        this.havingConditions = new LinkedHashSet<>(havingConditions);
    }

    public void setOrderConditions(LinkedHashSet<String> orderConditions) {
        this.orderConditions = orderConditions;
    }

    public void setOrderConditions(List<String> orderConditions) {
        this.orderConditions = new LinkedHashSet<>(orderConditions);
    }

    /**
     * Add the specified condition to the list of where conditions for the query.
     *
     * @param condition additional where condition
     */
    public void addWhereCondition(String condition) {
        if (Objects.isNull(whereConditions)) {
            whereConditions = new LinkedHashSet<>();
        }
        if (Objects.nonNull(condition) && !"".equals(condition.trim())) {
            whereConditions.add(condition);
        }
    }

    /**
     * Add the specified field to the list of group by fields for the query.
     *
     * @param field additional group by field
     */
    public void addGroupByField(String field) {
        if (Objects.isNull(groupByFields)) {
            groupByFields = new LinkedHashSet<>();
        }
        if (Objects.nonNull(field) && !"".equals(field.trim())) {
            groupByFields.add(field);
        }
    }

    /**
     * Add the specified condition to the list of having conditions for the query.
     *
     * @param condition additional having conditions
     */
    public void addHavingCondition(String condition) {
        if (Objects.isNull(havingConditions)) {
            havingConditions = new LinkedHashSet<>();
        }
        if (Objects.nonNull(condition) && !"".equals(condition.trim())) {
            havingConditions.add(condition);
        }
    }

    /**
     * Add the specified condition to the list of order conditions for the query.
     *
     * @param condition additional order condition
     */
    public void addOrderCondition(String condition) {
        if (Objects.isNull(orderConditions)) {
            orderConditions = new LinkedHashSet<>();
        }
        if (Objects.nonNull(condition) && !"".equals(condition.trim())) {
            orderConditions.add(condition);
        }
    }

    /**
     * Returns a clone of {@code this} object.
     *
     * @return the cloned {@link QueryParameter} object
     */
    @Override
    public QueryParameter clone() {
        QueryParameter clone = (QueryParameter) super.clone();
        clone.setSelectClause(selectClause);
        clone.setWhereConditions(whereConditions);
        clone.setGroupByFields(groupByFields);
        clone.setHavingConditions(havingConditions);
        clone.setOrderConditions(orderConditions);
        return clone;
    }
}
