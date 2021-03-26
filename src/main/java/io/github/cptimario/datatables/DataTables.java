package io.github.cptimario.datatables;

import io.github.cptimario.datatables.components.Column;
import io.github.cptimario.datatables.components.Order;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTables<E> {
    enum QueryType {
        RESULT_LIST, TOTAL_COUNT, FILTERED_COUNT
    }

    private final String entityName;
    private final DataTablesParameter dataTablesParameter;
    private Map<String, String> aliasMap;

    public static <E> DataTables<E> of(Class<E> entity, DataTablesParameter dataTablesParameter) {
        if (!entity.isAnnotationPresent(Entity.class))
            throw new IllegalArgumentException(entity.getName() + " is not a valid entity.");
        return new DataTables<>(entity, dataTablesParameter);
    }

    private DataTables(Class<E> entity, DataTablesParameter dataTablesParameter) {
        Objects.requireNonNull(dataTablesParameter);
        this.entityName = entity.getSimpleName();
        this.dataTablesParameter = dataTablesParameter;
        initializeAliasMap(entity);
    }

    /**
     * Returns the datatables response of this datatable
     *
     * @param entityManager the entity manager
     * @return the datatables response
     */
    public DataTablesResponse<E> getDataTablesResponse(EntityManager entityManager) {
        return getDataTablesResponse(entityManager, new QueryParameter());
    }

    /**
     * Returns the datatables response of this datatable with additional query parameters.
     *
     * @param entityManager the entity manager
     * @param queryParameter the additional query parameters
     * @return the datatables response
     */
    public DataTablesResponse<E> getDataTablesResponse(EntityManager entityManager, QueryParameter queryParameter) {
        DataTablesResponse<E> dataTablesResponse = new DataTablesResponse<>();
        List<E> resultList = getSearchResultList(entityManager, queryParameter);
        dataTablesResponse.setDraw(dataTablesParameter.getDraw());
        dataTablesResponse.setData(resultList);
        dataTablesResponse.setResultList(resultList);
        dataTablesResponse.setRecordsTotal(getRecordsTotalCount(entityManager, queryParameter));
        dataTablesResponse.setRecordsFiltered(getRecordsFilteredCount(entityManager, queryParameter));
        return dataTablesResponse;
    }

    @SuppressWarnings("unchecked")
    List<E> getSearchResultList(EntityManager entityManager, QueryParameter queryParameter) {
        QueryParameter resultListParameter = queryParameter.clone();
        String queryString = getQuery(resultListParameter, QueryType.RESULT_LIST);
        Query query = entityManager.createQuery(queryString);
        for (Map.Entry<String, Object> parameter : resultListParameter.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        query.setFirstResult(dataTablesParameter.getStart());
        query.setMaxResults(dataTablesParameter.getLength());
        return (List<E>) query.getResultList();
    }

    long getRecordsTotalCount(EntityManager entityManager, QueryParameter queryParameter) {
        QueryParameter totalCountParameter = queryParameter.clone();
        String queryString = getQuery(totalCountParameter, QueryType.TOTAL_COUNT);
        Query query = entityManager.createQuery(queryString);
        return (long) query.getSingleResult();
    }

    long getRecordsFilteredCount(EntityManager entityManager, QueryParameter queryParameter) {
        QueryParameter filteredCountParameter = queryParameter.clone();
        String queryString = getQuery(filteredCountParameter, QueryType.FILTERED_COUNT);
        Query query = entityManager.createQuery(queryString);
        for (Map.Entry<String, Object> parameter : filteredCountParameter.entrySet()) {
            query.setParameter(parameter.getKey(), parameter.getValue());
        }
        return (long) query.getSingleResult();
    }

    String getSelectCountClause(QueryParameter queryParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> groupByFieldsWithAlias = new HashSet<>();
        Set<String> groupByFields = queryParameter.getGroupByFields();
        stringBuilder.append("Select Count(");
        if (!groupByFields.isEmpty()) {
            stringBuilder.append(" Distinct ");
            for (String field : groupByFields) {
                groupByFieldsWithAlias.add(getClauseWithAlias(field));
            }
            stringBuilder.append(String.join(", ", groupByFieldsWithAlias));
        } else {
            stringBuilder.append(aliasMap.get(entityName));
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    String getQuery(QueryParameter queryParameter, QueryType queryType) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getSelectClause(queryParameter, queryType));
        stringBuilder.append(getFromClause(queryType));
        stringBuilder.append(getWhereClause(queryParameter, queryType));
        stringBuilder.append(getGroupByClause(queryParameter, queryType));
        stringBuilder.append(getOrderClause(queryParameter, queryType));
        return stringBuilder.toString();
    }

    private String getSelectClause(QueryParameter queryParameter, QueryType queryType) {
        if (!queryType.equals(QueryType.RESULT_LIST)) {
            return getSelectCountClause(queryParameter);
        } else if (queryParameter.getSelectClause().isEmpty()) {
            return "Select " + aliasMap.get(entityName);
        } else {
            return queryParameter.getSelectClause();
        }
    }

    String getFromClause(QueryType queryType) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append(entityName);
        stringBuilder.append(" ");
        stringBuilder.append(aliasMap.get(entityName));
        if (!queryType.equals(QueryType.TOTAL_COUNT)) {
            stringBuilder.append(" ");
            stringBuilder.append(getLeftJoinClause());
        }
        return stringBuilder.toString();
    }

    String getLeftJoinClause() {
        Set<String> leftJoinSet = new LinkedHashSet<>();
        String entityAlias = aliasMap.get(entityName);
        for (Map.Entry<String, String> aliasEntry : aliasMap.entrySet()) {
            if (!aliasEntry.getKey().equals(entityName)) {
                leftJoinSet.add("Left Join " + entityAlias + "." + aliasEntry.getKey() + " " + aliasEntry.getValue());
            }
        }
        return String.join(" ", leftJoinSet);
    }

    String getWhereClause(QueryParameter queryParameter, QueryType queryType) {
        Set<String> whereConditionsWithAlias = new LinkedHashSet<>();
        Set<String> whereConditions = queryParameter.getWhereConditions();
        if (!queryType.equals(QueryType.TOTAL_COUNT)) {
            addSearchCondition(whereConditionsWithAlias, queryParameter);
        }
        for (String condition : whereConditions) {
            whereConditionsWithAlias.add(getClauseWithAlias(condition));
        }
        if (!whereConditionsWithAlias.isEmpty()) {
            return " Where " + String.join(" And ", whereConditionsWithAlias);
        }
        return "";
    }

    private void addSearchCondition(Set<String> whereConditions, QueryParameter queryParameter) {
        String searchCondition = getSearchCondition(queryParameter);
        if (!"".equals(searchCondition)) {
            whereConditions.add(searchCondition);
        }
    }

    String getGroupByClause(QueryParameter queryParameter, QueryType queryType) {
        if (queryType.equals(QueryType.RESULT_LIST)) {
            return getGroupByClause(queryParameter) + getHavingClause(queryParameter);
        }
        return "";
    }

    private String getGroupByClause(QueryParameter queryParameter) {
        Set<String> groupByFieldsWithAlias = new LinkedHashSet<>();
        Set<String> groupByFields = queryParameter.getGroupByFields();
        for (String condition : groupByFields) {
            groupByFieldsWithAlias.add(getClauseWithAlias(condition));
        }
        if (!groupByFieldsWithAlias.isEmpty()) {
            return " Group By " + String.join(", ", groupByFieldsWithAlias);
        }
        return "";
    }

    private String getHavingClause(QueryParameter queryParameter) {
        Set<String> havingConditionsWithAlias = new LinkedHashSet<>();
        Set<String> havingConditions = queryParameter.getHavingConditions();
        for (String condition : havingConditions) {
            havingConditionsWithAlias.add(getClauseWithAlias(condition));
        }
        if (!havingConditionsWithAlias.isEmpty()) {
            return " Having " + String.join(" And ", havingConditionsWithAlias);
        }
        return "";
    }

    String getOrderClause(QueryParameter queryParameter, QueryType queryType) {
        if (queryType.equals(QueryType.RESULT_LIST)) {
            Set<String> orderConditionsWithAlias = getOrderableColumnsConditions();
            Set<String> orderConditions = queryParameter.getOrderConditions();
            for (String condition : orderConditions)
                orderConditionsWithAlias.add(getClauseWithAlias(condition));
            if (!orderConditionsWithAlias.isEmpty())
                return " Order By " + String.join(", ", orderConditionsWithAlias);
        }
        return "";
    }

    private Set<String> getOrderableColumnsConditions() {
        Set<String> orderableColumnsConditions = new LinkedHashSet<>();
        for (Order order : dataTablesParameter.getOrder()) {
            int index = order.getColumn();
            io.github.cptimario.datatables.components.Column column = dataTablesParameter.getColumns().get(index);
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
        if (matcher.find()) {
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
        for (io.github.cptimario.datatables.components.Column column : dataTablesParameter.getColumns()) {
            String searchString = getSearchString(column);
            if (column.isSearchable() && !"".equals(searchString)) {
                String fieldName = getQueryFieldName(column, true);
                String namedParameter = "value_" + queryParameter.size();
                String fieldQuery = getFieldQuery(fieldName, namedParameter);
                searchQueryList.add(fieldQuery);
                queryParameter.put(namedParameter, escapeWildcards(searchString));
            }
        }
        if (!searchQueryList.isEmpty()) {
            stringBuilder.append("(");
            stringBuilder.append(String.join(" Or ", searchQueryList));
            stringBuilder.append(")");
        }
        return stringBuilder.toString();
    }

    String escapeWildcards(String searchText) {
        return searchText.replaceAll("([#%_])", "#$1");
    }

    String getFieldQuery(String fieldName, String namedParameter) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Upper(");
        stringBuilder.append(fieldName);
        stringBuilder.append(") ");
        stringBuilder.append("Like ");
        stringBuilder.append("Upper(");
        stringBuilder.append("Concat('%', :");
        stringBuilder.append(namedParameter);
        stringBuilder.append(", '%')");
        stringBuilder.append(") ");
        stringBuilder.append("Escape '#'");
        return stringBuilder.toString();
    }

    String getQueryFieldName(io.github.cptimario.datatables.components.Column column) {
        return getQueryFieldName(column, false);
    }

    String getQueryFieldName(io.github.cptimario.datatables.components.Column column, boolean isFormatted) {
        if (!column.isMultiField()) {
            return getSingleFieldColumnQueryFieldName(column, isFormatted);
        }
        return getMultiFieldColumnQueryFieldName(column, isFormatted);
    }

    private String getSingleFieldColumnQueryFieldName(io.github.cptimario.datatables.components.Column column, boolean isFormatted) {
        StringBuilder stringBuilder = new StringBuilder();
        String fieldName = column.getData();
        String alias = getColumnAlias(column);
        stringBuilder.append(alias);
        stringBuilder.append(".");
        int startIndex = fieldName.indexOf(".") + 1;
        fieldName = fieldName.substring(startIndex);
        stringBuilder.append(fieldName);
        if (isFormatted && Objects.nonNull(column.getFormat())) {
            return "function('date_format', " + stringBuilder.toString() + ", '" + column.getFormat() + "')";
        }
        return stringBuilder.toString();
    }

    private String getMultiFieldColumnQueryFieldName(io.github.cptimario.datatables.components.Column column, boolean isFormatted) {
        List<String> queryFieldNameList = new ArrayList<>();
        String fieldDelimiter = column.getFieldDelimiter();
        for (io.github.cptimario.datatables.components.Column subColumn : column.getSubColumnList()) {
            String queryFieldName = getSingleFieldColumnQueryFieldName(subColumn, isFormatted);
            queryFieldNameList.add(queryFieldName);
        }
        return "Concat(" + String.join(", '" + fieldDelimiter + "', ", queryFieldNameList) + ")";
    }

    String getSearchString(io.github.cptimario.datatables.components.Column column) {
        if (!"".equals(column.getSearchValue())) {
            return column.getSearchValue();
        }
        return dataTablesParameter.getSearchValue();
    }

    String getColumnAlias(Column column) {
        if (column.hasRelationship()) {
            return aliasMap.get(column.getBaseField());
        }
        return aliasMap.get(entityName);
    }

    private void initializeAliasMap(Class<E> entity) {
        aliasMap = new LinkedHashMap<>();
        aliasMap.put(entityName, getCamelCase(entityName));
        Field[] entityFields = entity.getDeclaredFields();
        for (Field field : entityFields) {
            registerAlias(field);
        }
    }

    private void registerAlias(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            registerAlias(field.getName());
        }
    }

    private void registerAlias(String fieldName) {
        if (Objects.isNull(aliasMap.get(fieldName))) {
            String alias = getAliasPrefix(fieldName) + "_" + aliasMap.size();
            aliasMap.put(fieldName, alias);
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
