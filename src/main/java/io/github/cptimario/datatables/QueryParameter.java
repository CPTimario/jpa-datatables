package io.github.cptimario.datatables;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * DataTablesResponse class handles the additional query parameters needed in generating the datatables response
 *
 * @author Christopher Timario
 * @version v1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryParameter extends HashMap<String, Object> {
    private String selectClause;
    private LinkedHashSet<String> whereConditions;
    private LinkedHashSet<String> groupByFields;
    private LinkedHashSet<String> havingConditions;
    private LinkedHashSet<String> orderConditions;

    /**
     * Create an instance of query parameters with default values
     */
    public QueryParameter() {
        setSelectClause("");
        setWhereConditions(new LinkedHashSet<>());
        setGroupByFields(new LinkedHashSet<>());
        setHavingConditions(new LinkedHashSet<>());
        setOrderConditions(new LinkedHashSet<>());
    }

    /**
     * Add the specified condition to the list of where conditions for the query.
     *
     * @param condition additional where condition
     */
    public void addWhereCondition(String condition) {
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
