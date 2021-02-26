package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;

import javax.persistence.Entity;
import java.util.*;

public class DataTables<E> {
    private final Class<E> entity;
    private final String entityName;
    private JoinType joinType;
    private DataTablesParameter dataTablesParameter;
    private HashMap<String, String> aliasMap;
    private HashMap<String, String> namedParameterMap;

    public DataTables(Class<E> entity, DataTablesParameter dataTablesParameter) {
        this(entity, dataTablesParameter, JoinType.CROSS_JOIN);
    }

    public DataTables(Class<E> entity, DataTablesParameter dataTablesParameter, JoinType joinType) {
        if (!isEntity(entity))
            throw new IllegalArgumentException(entity.getName() + " is not a valid entity.");
        Objects.requireNonNull(dataTablesParameter);
        this.entity = entity;
        this.joinType = joinType;
        this.entityName = entity.getSimpleName();
        this.dataTablesParameter = dataTablesParameter;
        aliasMap = new HashMap<>();
        namedParameterMap = new HashMap<>();
        registerAliasMap();
    }

    static <E> boolean isEntity(Class<E> entity) {
        return Objects.nonNull(entity.getAnnotation(Entity.class));
    }

    public String getQueryString(QueryParameter queryParameter) {

        return "";
    }

    public List<String> getSearchQueryList(QueryParameter queryParameter) {
        String searchString, fieldName, namedParameter, fieldQuery;
        List<String> searchQueryList = new ArrayList<>();
        this.namedParameterMap = queryParameter;
        for (Column column : dataTablesParameter.getColumnList()) {
            searchString = getSearchString(column);
            if (column.isSearchable() && !"".equals(searchString)) {
                fieldName = getQueryFieldName(column);
                namedParameter = "val_" + namedParameterMap.size();
                fieldQuery = getFieldQuery(fieldName, namedParameter);
                searchQueryList.add(fieldQuery);
                namedParameterMap.put(namedParameter, searchString);
            }
        }
        return searchQueryList;
    }

    String getFieldQuery(String fieldName, String namedParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fieldName);
        stringBuilder.append(" LIKE CONCAT('%', :");
        stringBuilder.append(namedParameter);
        stringBuilder.append(", '%') ESCAPE '#' ");
        return stringBuilder.toString();
    }

    String getQueryFieldName(Column column) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!column.isMultiField()) {
            String alias = getColumnAlias(column);
            stringBuilder.append(alias);
            stringBuilder.append(".");
            stringBuilder.append(column.getFullFieldName());
        } else {
            List<Column> subColumnList = column.getSubColumnList();
            int lastSubColumnIndex = subColumnList.size() - 1;
            stringBuilder.append(" CONCAT ( ");
            for (Column subColumn : subColumnList) {
                String queryFieldName = getQueryFieldName(subColumn);
                stringBuilder.append(queryFieldName);
                if (subColumnList.indexOf(subColumn) < lastSubColumnIndex)
                    stringBuilder.append(", ' ',");
            }
            stringBuilder.append(" ) ");
        }
        return stringBuilder.toString();
    }

    String getSearchString(Column column) {
        if (!"".equals(column.getSearchValue()))
            return column.getSearch().getValue();
        return dataTablesParameter.getSearch().getValue();
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
