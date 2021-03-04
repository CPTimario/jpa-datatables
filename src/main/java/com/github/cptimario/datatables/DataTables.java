package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;
import com.github.cptimario.datatables.components.Order;
import com.github.cptimario.datatables.components.QueryType;
import org.hibernate.Session;

import javax.persistence.Query;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTables<E> {

    private final String entityName;
    private final JoinType joinType;
    private final DataTablesParameter dataTablesParameter;
    private final Map<String, String> aliasMap;

    public static <E> DataTables<E> of(Class<E> entity, DataTablesParameter dataTablesParameter) {
        return new DataTables<>(entity, dataTablesParameter, JoinType.CROSS_JOIN);
    }

    public static <E> DataTables<E> of(Class<E> entity, DataTablesParameter dataTablesParameter, JoinType joinType) {
        return new DataTables<>(entity, dataTablesParameter, joinType);
    }

    private DataTables(Class<E> entity, DataTablesParameter dataTablesParameter, JoinType joinType) {
        Objects.requireNonNull(dataTablesParameter);
        this.entityName = entity.getSimpleName();
        this.dataTablesParameter = dataTablesParameter;
        this.joinType = joinType;
        this.aliasMap = new HashMap<>();
        registerAliasMap();
    }

    public DataTablesResponse<E> getDataTablesResponse(QueryParameter queryParameter) {
        DataTablesResponse<E> dataTablesResponse = new DataTablesResponse<>();
        List<E> resultList = getSearchResultList(queryParameter);
        dataTablesResponse.setDraw(dataTablesParameter.getDraw());
        dataTablesResponse.setData(resultList);
        dataTablesResponse.setResultList(resultList);
        dataTablesResponse.setRecordsTotal(getRecordsTotalCount(queryParameter));
        dataTablesResponse.setRecordsFiltered(getRecordsFilteredCount(queryParameter));
        return dataTablesResponse;
    }

    @SuppressWarnings("unchecked")
    public List<E> getSearchResultList(QueryParameter queryParameter) {
        QueryParameter resultListParameter = queryParameter.clone();
        Session session = queryParameter.getSession();
        Query query = session.createQuery(getQuery(resultListParameter, QueryType.RESULT_LIST));
        for (Map.Entry<String, Object> parameter : resultListParameter.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        query.setFirstResult(dataTablesParameter.getStart());
        query.setMaxResults(dataTablesParameter.getLength());
        return (List<E>) query.getResultList();
    }

    public long getRecordsTotalCount(QueryParameter queryParameter) {
        QueryParameter totalCountParameter = queryParameter.clone();
        Session session = queryParameter.getSession();
        Query query = session.createQuery(getQuery(totalCountParameter, QueryType.TOTAL_COUNT));
        for (Map.Entry<String, Object> parameter : totalCountParameter.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        return (long) query.getSingleResult();
    }

    public long getRecordsFilteredCount(QueryParameter queryParameter) {
        QueryParameter filteredCountParameter = queryParameter.clone();
        Session session = queryParameter.getSession();
        Query query = session.createQuery(getQuery(filteredCountParameter, QueryType.FILTERED_COUNT));
        for (Map.Entry<String, Object> parameter : filteredCountParameter.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        return (long) query.getSingleResult();
    }

    String getSelectCountClause(QueryParameter queryParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> groupByFieldsWithAlias = new HashSet<>();
        Set<String> groupByFields = queryParameter.getGroupByFields();
        stringBuilder.append(" Select Count(");
        if (!groupByFields.isEmpty()) {
            stringBuilder.append(" Distinct ");
            for (String field : groupByFields) {
                groupByFieldsWithAlias.add(getClauseWithAlias(field));
            }
            stringBuilder.append(String.join(", ", groupByFieldsWithAlias));
        } else {
            stringBuilder.append("*");
        }
        stringBuilder.append(") ");
        return stringBuilder.toString();
    }

    String getQuery(QueryParameter queryParameter, QueryType queryType) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getSelectClause(queryParameter, queryType));
        stringBuilder.append(getFromClause());
        stringBuilder.append(getWhereClause(queryParameter, queryType));
        stringBuilder.append(getGroupByClause(queryParameter, queryType));
        stringBuilder.append(getOrderClause(queryParameter));
        return stringBuilder.toString();
    }

    private String getSelectClause(QueryParameter queryParameter, QueryType queryType) {
        if (!queryType.equals(QueryType.RESULT_LIST))
            return getSelectCountClause(queryParameter);
        else if (queryParameter.getSelectClause().isEmpty())
            return " Select " + aliasMap.get(entityName);
        else
            return queryParameter.getSelectClause();
    }

    String getFromClause() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append(entityName);
        stringBuilder.append(" ");
        stringBuilder.append(aliasMap.get(entityName));
        if (joinType.equals(JoinType.LEFT_JOIN)) {
            stringBuilder.append(getLeftJoinClause(dataTablesParameter.getColumns()));
        }
        return stringBuilder.toString();
    }

    String getLeftJoinClause(List<Column> columnList) {
        Set<String> leftJoinSet = new LinkedHashSet<>();
        for (Column column : columnList) {
            if (column.isMultiField()) {
                for (Column subColumn : column.getSubColumnList()) {
                    addLeftJoinClause(leftJoinSet, subColumn);
                }
            } else {
                addLeftJoinClause(leftJoinSet, column);
            }
        }
        return String.join(" ", leftJoinSet);
    }

    private void addLeftJoinClause(Set<String> leftJoinSet, Column column) {
        if (column.hasRelationship()) {
            String fieldName = column.getEntityField();
            registerAlias(fieldName);
            String mainEntityAlias = aliasMap.get(entityName);
            String entityFieldAlias = aliasMap.get(fieldName);
            String leftJoinClause = column.getLeftJoinClause(mainEntityAlias, entityFieldAlias);
            leftJoinSet.add(leftJoinClause);
        }
    }

    String getWhereClause(QueryParameter queryParameter, QueryType queryType) {
        Set<String> whereConditionsWithAlias = new LinkedHashSet<>();
        Set<String> whereConditions = queryParameter.getWhereConditions();
        if (!queryType.equals(QueryType.TOTAL_COUNT))
            addSearchCondition(whereConditionsWithAlias, queryParameter);
        for (String condition : whereConditions)
            whereConditionsWithAlias.add(getClauseWithAlias(condition));
        if (!whereConditionsWithAlias.isEmpty())
            return " Where " + String.join(" And ", whereConditionsWithAlias);
        return "";
    }

    private void addSearchCondition(Set<String> whereConditions, QueryParameter queryParameter) {
        String searchCondition = getSearchCondition(queryParameter);
        if (!"".equals(searchCondition))
            whereConditions.add(searchCondition);
    }

    String getGroupByClause(QueryParameter queryParameter, QueryType queryType) {
        if (queryType.equals(QueryType.RESULT_LIST))
            return getGroupByClause(queryParameter) + getHavingClause(queryParameter);
        return "";
    }

    private String getGroupByClause(QueryParameter queryParameter) {
        Set<String> groupByFieldsWithAlias = new LinkedHashSet<>();
        Set<String> groupByFields = queryParameter.getGroupByFields();
        for (String condition : groupByFields)
            groupByFieldsWithAlias.add(getClauseWithAlias(condition));
        if (!groupByFieldsWithAlias.isEmpty())
            return " Group By " + String.join(", ", groupByFieldsWithAlias);
        return "";
    }

    private String getHavingClause(QueryParameter queryParameter) {
        Set<String> havingConditionsWithAlias = new LinkedHashSet<>();
        Set<String> havingConditions = queryParameter.getHavingConditions();
        for (String condition : havingConditions)
            havingConditionsWithAlias.add(getClauseWithAlias(condition));
        if (!havingConditionsWithAlias.isEmpty())
            return " Having " + String.join(" And ", havingConditionsWithAlias);
        return "";
    }

    String getOrderClause(QueryParameter queryParameter) {
        Set<String> orderConditionsWithAlias = getOrderableColumnsConditions();
        Set<String> orderConditions = queryParameter.getOrderConditions();
        for (String condition : orderConditions)
            orderConditionsWithAlias.add(getClauseWithAlias(condition));
        if (!orderConditionsWithAlias.isEmpty())
            return " Order By " + String.join(", ", orderConditionsWithAlias);
        return "";
    }

    private Set<String> getOrderableColumnsConditions() {
        Set<String> orderableColumnsConditions = new LinkedHashSet<>();
        for (Order order : dataTablesParameter.getOrder()) {
            int index = order.getColumn();
            Column column = dataTablesParameter.getColumns().get(index);
            if (column.isOrderable()) {
                orderableColumnsConditions.add(getOrderCondition(order));
            }
        }
        return orderableColumnsConditions;
    }

    String getClauseWithAlias(String field) {
        String entityAlias = aliasMap.get(entityName);
        String regex = entityAlias + "\\.(?<field>\\w+)(?:\\.\\w+)+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(field);
        boolean isLeftJoin = joinType.equals(JoinType.LEFT_JOIN);
        if (isLeftJoin && matcher.find()) {
            String fieldName = matcher.group("field");
            String alias = aliasMap.get(fieldName);
            return field.replace(entityAlias + "." + fieldName, alias);
        }
        return field;
    }

    private String getOrderCondition(Order order) {
        int index = order.getColumn();
        Column column = dataTablesParameter.getColumns().get(index);
        String fieldName = getQueryFieldName(column);
        return getClauseWithAlias(fieldName + " " + order.getDir());
    }

    String getSearchCondition(QueryParameter queryParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> searchQueryList = new ArrayList<>();
        for (Column column : dataTablesParameter.getColumns()) {
            String searchString = getSearchString(column);
            if (column.isSearchable() && !"".equals(searchString)) {
                String fieldName = getQueryFieldName(column, true);
                String namedParameter = "value_" + queryParameter.size();
                String fieldQuery = getFieldQuery(fieldName, namedParameter);
                searchQueryList.add(fieldQuery);
                queryParameter.put(namedParameter, searchString);
            }
        }
        if (!searchQueryList.isEmpty()) {
            stringBuilder.append(" ( ");
            stringBuilder.append(String.join(" Or ", searchQueryList));
            stringBuilder.append(" ) ");
        }
        return stringBuilder.toString();
    }

    String getFieldQuery(String fieldName, String namedParameter) {
        return fieldName + " LIKE CONCAT('%', :" + namedParameter + ", '%') ESCAPE '#' ";
    }

    String getQueryFieldName(Column column) {
        return getQueryFieldName(column, false);
    }

    String getQueryFieldName(Column column, boolean isFormatted) {
        if (!column.isMultiField())
            return getSingleFieldColumnQueryFieldName(column, isFormatted);
        return getMultiFieldColumnQueryFieldName(column, isFormatted);
    }

    private String getSingleFieldColumnQueryFieldName(Column column, boolean isFormatted) {
        StringBuilder stringBuilder = new StringBuilder();
        String fieldName = column.getField();
        String alias = getColumnAlias(column);
        stringBuilder.append(alias);
        stringBuilder.append(".");
        if (joinType.equals(JoinType.LEFT_JOIN)) {
            int startIndex = fieldName.indexOf(".") + 1;
            fieldName = fieldName.substring(startIndex);
        }
        stringBuilder.append(fieldName);
        if (isFormatted && Objects.nonNull(column.getFormat()))
            return "function('date_format', " + stringBuilder.toString() + ", '" + column.getFormat() + "')";
        return stringBuilder.toString();
    }

    private String getMultiFieldColumnQueryFieldName(Column column, boolean isFormatted) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Column> subColumnList = column.getSubColumnList();
        int lastSubColumnIndex = subColumnList.size() - 1;
        stringBuilder.append(" CONCAT ( ");
        for (Column subColumn : subColumnList) {
            String queryFieldName = getSingleFieldColumnQueryFieldName(subColumn, isFormatted);
            stringBuilder.append(queryFieldName);
            if (subColumnList.indexOf(subColumn) < lastSubColumnIndex)
                stringBuilder.append(", ' ',");
        }
        stringBuilder.append(" ) ");
        return stringBuilder.toString();
    }

    String getSearchString(Column column) {
        if (!"".equals(column.getSearchValue()))
            return column.getSearchValue();
        return dataTablesParameter.getSearchValue();
    }

    String getColumnAlias(Column column) {
        boolean isLeftJoin = joinType.equals(JoinType.LEFT_JOIN);
        if (isLeftJoin && column.hasRelationship())
            return aliasMap.get(column.getEntityField());
        return aliasMap.get(entityName);
    }

    private void registerAliasMap() {
        aliasMap.put(entityName, getCamelCase(entityName));
        for (Column column : dataTablesParameter.getColumns()) {
            registerAlias(column);
        }
    }

    private void registerAlias(Column column) {
        if (column.isMultiField()) {
            for (Column subColumn : column.getSubColumnList()) {
                registerAlias(subColumn);
            }
        } else if (column.hasRelationship() && joinType.equals(JoinType.LEFT_JOIN)) {
            registerAlias(column.getEntityField());
        }
    }

    private void registerAlias(String fieldName) {
        if (Objects.isNull(aliasMap.get(fieldName))) {
            StringBuilder stringBuilder = new StringBuilder();
            String[] relationshipFieldNames = fieldName.split("\\.");
            for (String field : relationshipFieldNames) {
                stringBuilder.append(getAliasPrefix(field));
                stringBuilder.append("_");
            }
            stringBuilder.append(aliasMap.size());
            aliasMap.put(fieldName, stringBuilder.toString());
        }
    }

    private String getCamelCase(String text) {
        if (Objects.nonNull(text) && !"".equals(text.trim()))
            return Character.toLowerCase(text.charAt(0)) + text.substring(1);
        return "";
    }

    private String getAliasPrefix(String field) {
        String prefix = field.length() > 5 ? field.substring(0, 5) : field;
        return getCamelCase(prefix);
    }
}
