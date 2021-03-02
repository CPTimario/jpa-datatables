package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;
import com.github.cptimario.datatables.components.Order;
import org.hibernate.Session;

import javax.persistence.Query;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTables<E> {
    private final String entityName;
    private final JoinType joinType;
    private final DataTablesParameter dataTablesParameter;
    private final HashMap<String, String> aliasMap;
    private HashMap<String, Object> namedParameterMap;

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
        Session session = queryParameter.getSession();
        Query query = session.createQuery(getQuery(queryParameter, true));
        for (Map.Entry<String, Object> parameter : namedParameterMap.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        query.setFirstResult(dataTablesParameter.getStart());
        query.setMaxResults(dataTablesParameter.getLength());
        return (List<E>) query.getResultList();
    }

    public long getRecordsTotalCount(QueryParameter queryParameter) {
        Session session = queryParameter.getSession();
        queryParameter.setSelectClause(getCountQuery(queryParameter));
        queryParameter.setGroupByFields(Collections.emptySet());
        Query query = session.createQuery(getQuery(queryParameter, false));
        for (Map.Entry<String, Object> parameter : namedParameterMap.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        return (long) query.getSingleResult();
    }

    public long getRecordsFilteredCount(QueryParameter queryParameter) {
        Session session = queryParameter.getSession();
        queryParameter.setSelectClause(getCountQuery(queryParameter));
        queryParameter.setGroupByFields(Collections.emptySet());
        Query query = session.createQuery(getQuery(queryParameter, true));
        for (Map.Entry<String, Object> parameter : namedParameterMap.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        return (long) query.getSingleResult();
    }

    String getCountQuery(QueryParameter queryParameter) {
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

    public String getQuery(QueryParameter queryParameter, boolean isSearch) {
        StringBuilder stringBuilder = new StringBuilder();
        addOrderableColumns(queryParameter);
        if (queryParameter.getSelectClause().isEmpty()) {
            queryParameter.setSelectClause(" Select " + aliasMap.get(entityName));
        }
        stringBuilder.append(queryParameter.getSelectClause());
        stringBuilder.append(getFromClause());
        stringBuilder.append(getWhereClause(queryParameter, isSearch));
        stringBuilder.append(getGroupByClause(queryParameter));
        stringBuilder.append(getHavingClause(queryParameter));
        stringBuilder.append(getOrderClause(queryParameter));
        return stringBuilder.toString();
    }

    String getWhereClause(QueryParameter queryParameter, boolean isSearch) {
        Set<String> whereConditionsWithAlias = new LinkedHashSet<>();
        Set<String> whereConditions = queryParameter.getWhereConditions();
        for (String condition : whereConditions)
            whereConditionsWithAlias.add(getClauseWithAlias(condition));
        queryParameter.setWhereConditions(whereConditionsWithAlias);
        if (isSearch)
            queryParameter.addWhereCondition(getSearchCondition(queryParameter));
        return queryParameter.getWhereClause();
    }

    String getGroupByClause(QueryParameter queryParameter) {
        Set<String> groupByFieldsWithAlias = new LinkedHashSet<>();
        Set<String> groupByFields = queryParameter.getGroupByFields();
        for (String condition : groupByFields)
            groupByFieldsWithAlias.add(getClauseWithAlias(condition));
        queryParameter.setGroupByFields(groupByFieldsWithAlias);
        return queryParameter.getGroupByClause();
    }

    String getHavingClause(QueryParameter queryParameter) {
        Set<String> havingConditionsWithAlias = new LinkedHashSet<>();
        Set<String> havingConditions = queryParameter.getHavingConditions();
        for (String condition : havingConditions)
            havingConditionsWithAlias.add(getClauseWithAlias(condition));
        queryParameter.setHavingConditions(havingConditionsWithAlias);
        return queryParameter.getHavingClause();
    }

    String getOrderClause(QueryParameter queryParameter) {
        Set<String> orderConditionsWithAlias = new LinkedHashSet<>();
        Set<String> orderConditions = queryParameter.getOrderConditions();
        for (String condition : orderConditions)
            orderConditionsWithAlias.add(getClauseWithAlias(condition));
        queryParameter.setOrderConditions(orderConditionsWithAlias);
        return queryParameter.getOrderByClause();
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

    private void addOrderableColumns(QueryParameter queryParameter) {
        for (Order order : dataTablesParameter.getOrder()) {
            int index = order.getColumn();
            Column column = dataTablesParameter.getColumns().get(index);
            if (column.isOrderable())
                queryParameter.addOrderCondition(getOrderQuery(order));
        }
    }

    private String getOrderQuery(Order order) {
        int index = order.getColumn();
        Column column = dataTablesParameter.getColumns().get(index);
        String fieldName = getQueryFieldName(column);
        return fieldName + " " + order.getDir();
    }

    public String getSearchCondition(QueryParameter queryParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> searchQueryList = new ArrayList<>();
        namedParameterMap = new HashMap<>(queryParameter);
        for (Column column : dataTablesParameter.getColumns()) {
            String searchString = getSearchString(column);
            if (column.isSearchable() && !"".equals(searchString)) {
                String fieldName = getQueryFieldName(column);
                String namedParameter = "value_" + namedParameterMap.size();
                String fieldQuery = getFieldQuery(fieldName, namedParameter);
                searchQueryList.add(fieldQuery);
                namedParameterMap.put(namedParameter, searchString);
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
        if (!column.isMultiField())
            return getSingleFieldColumnQueryFieldName(column);
        return getMultiFieldColumnQueryFieldName(column);
    }

    private String getSingleFieldColumnQueryFieldName(Column column) {
        StringBuilder stringBuilder = new StringBuilder();
        String fieldName = column.getFullFieldName();
        String alias = getColumnAlias(column);
        stringBuilder.append(alias);
        stringBuilder.append(".");
        if (joinType.equals(JoinType.LEFT_JOIN)) {
            int startIndex = fieldName.indexOf(".") + 1;
            fieldName = fieldName.substring(startIndex);
        }
        stringBuilder.append(fieldName);
        if (Objects.nonNull(column.getFormat()))
            return "Format(" + stringBuilder.toString() + ", '" + column.getFormat() + "')";
        return stringBuilder.toString();
    }

    private String getMultiFieldColumnQueryFieldName(Column column) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Column> subColumnList = column.getSubColumnList();
        int lastSubColumnIndex = subColumnList.size() - 1;
        stringBuilder.append(" CONCAT ( ");
        for (Column subColumn : subColumnList) {
            String queryFieldName = getSingleFieldColumnQueryFieldName(subColumn);
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
            return aliasMap.get(column.getRelationshipFieldName());
        return aliasMap.get(entityName);
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
            String leftJoinClause, leftTableAlias, rightTableAlias;
            String fieldName = column.getRelationshipFieldName();
            registerAlias(fieldName);
            leftTableAlias = aliasMap.get(entityName);
            rightTableAlias = aliasMap.get(fieldName);
            leftJoinClause = column.getLeftJoinClause(leftTableAlias, rightTableAlias);
            leftJoinSet.add(leftJoinClause);
        }
    }

    private void registerAliasMap() {
        aliasMap.put(entityName, entityName.toLowerCase());
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
            registerAlias(column.getRelationshipFieldName());
        }
    }

    private void registerAlias(String fieldName) {
        int currentAliasCount = aliasMap.size();
        int aliasSuffix = Objects.isNull(aliasMap.get(fieldName)) ? currentAliasCount : ++currentAliasCount;
        String aliasPrefix = fieldName.length() > 5 ? fieldName.substring(0, 5) : fieldName;
        String alias = aliasPrefix.toLowerCase() + "_" + aliasSuffix;
        if (Objects.isNull(aliasMap.get(fieldName)))
            aliasMap.put(fieldName, alias);
    }
}
