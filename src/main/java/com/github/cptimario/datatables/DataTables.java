package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;
import com.github.cptimario.datatables.components.Order;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.Query;
import java.util.*;

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
        this.joinType = joinType;
        this.entityName = entity.getSimpleName();
        this.dataTablesParameter = dataTablesParameter;
        this.aliasMap = new HashMap<>();
        registerAliasMap();
    }

    public DataTablesResponse<E> getDataTablesResponse(QueryParameter queryParameter) {
        DataTablesResponse<E> dataTablesResponse = new DataTablesResponse<>();
        dataTablesResponse.setDraw(dataTablesParameter.getDraw());
        dataTablesResponse.setData(getSearchResultList(queryParameter));
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

    public int getRecordsTotalCount(QueryParameter queryParameter) {
        Session session = queryParameter.getSession();
        queryParameter.setSelectClause(" Select Count(*) ");
        Query query = session.createQuery(getQuery(queryParameter, false));
        return (int) query.getSingleResult();
    }

    public int getRecordsFilteredCount(QueryParameter queryParameter) {
        Session session = queryParameter.getSession();
        queryParameter.setSelectClause(" Select Count(*) ");
        Query query = session.createQuery(getQuery(queryParameter, true));
        return (int) query.getSingleResult();
    }

    public String getQuery(QueryParameter queryParameter, boolean isSearch) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isSearch) {
            queryParameter.addWhereCondition(getSearchCondition(queryParameter));
            addOrderableColumns(queryParameter);
        }
        stringBuilder.append(queryParameter.getSelectClause());
        stringBuilder.append(getFromClause());
        stringBuilder.append(queryParameter.getWhereClause());
        stringBuilder.append(queryParameter.getGroupByClause());
        stringBuilder.append(queryParameter.getHavingClause());
        stringBuilder.append(queryParameter.getOrderByClause());
        return stringBuilder.toString();
    }

    private void addOrderableColumns(QueryParameter queryParameter) {
        for (Order order : dataTablesParameter.getOrderList()) {
            int index = order.getColumn();
            Column column = dataTablesParameter.getColumnList().get(index);
            if (column.isOrderable())
                queryParameter.addOrderCondition(getOrderQuery(order));
        }
    }

    private String getOrderQuery(Order order) {
        int index = order.getColumn();
        Column column = dataTablesParameter.getColumnList().get(index);
        String fieldName = getQueryFieldName(column);
        return fieldName + " " + order.getDir();
    }

    public String getSearchCondition(QueryParameter queryParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> searchQueryList = new ArrayList<>();
        namedParameterMap = new HashMap<>(queryParameter);
        for (Column column : dataTablesParameter.getColumnList()) {
            String searchString = getSearchString(column);
            if (column.isSearchable() && !"".equals(searchString)) {
                String fieldName = getQueryFieldName(column);
                String namedParameter = "value_" + namedParameterMap.size();
                String fieldQuery = getFieldQuery(fieldName, namedParameter);
                searchQueryList.add(fieldQuery);
                namedParameterMap.put(namedParameter, searchString);
            }
        }
        stringBuilder.append(" ( ");
        stringBuilder.append(String.join(" Or ", searchQueryList));
        stringBuilder.append(" ) ");
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
            stringBuilder.append(getLeftJoinClause(dataTablesParameter.getColumnList()));
        }
        return stringBuilder.toString();
    }

    String getLeftJoinClause(List<Column> columnList) {
        Set<String> leftJoinSet = new LinkedHashSet<>();
        for (Column column : columnList) {
            if (column.isMultiField()) {
                for (Column subColumn : column.getSubColumnList()) {
                    leftJoinSet.add(getLeftJoinClause(subColumn));
                }
            } else if (column.hasRelationship()) {
                leftJoinSet.add(getLeftJoinClause(column));
            }
        }
        return String.join(" ", leftJoinSet);
    }

    String getLeftJoinClause(Column column) {
        String leftTableAlias, rightTableAlias;
        String fieldName = column.getRelationshipFieldName();
        registerAlias(fieldName);
        leftTableAlias = aliasMap.get(entityName);
        rightTableAlias = aliasMap.get(fieldName);
        return column.getLeftJoinClause(leftTableAlias, rightTableAlias);
    }

    private void registerAliasMap() {
        registerAlias(entityName);
        for (Column column : dataTablesParameter.getColumnList()) {
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
