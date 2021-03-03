package com.github.cptimario.datatables.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Column {
    private String data;
    private boolean searchable;
    private boolean orderable;
    private Search search;
    private String format;

    public Column() {
        this.data = "";
        this.searchable = true;
        this.search = new Search();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isOrderable() {
        return orderable;
    }

    public void setOrderable(boolean orderable) {
        this.orderable = orderable;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public String getSearchValue() {
        return search.getValue();
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isMultiField() {
        return Objects.nonNull(data) && data.contains("+");
    }

    public boolean hasRelationship() {
        return Objects.nonNull(data) && data.contains(".");
    }

    public boolean isNullable() {
        return Objects.nonNull(data) && data.contains("?");
    }

    public String getFullFieldName() {
        if (isMultiField())
            return String.join(" ", getFullFieldNameList());
        return String.join(".", getFieldNameList());
    }

    public List<String> getFullFieldNameList() {
        if (!isMultiField())
            throw new IllegalCallerException("Column '" + getFullFieldName() + "' is not a multi-field column.");
        List<String> fullFieldNameList = new ArrayList<>();
        for (Column subColumn : getSubColumnList()) {
            fullFieldNameList.add(subColumn.getFullFieldName());
        }
        return fullFieldNameList;
    }

    public String getRelationshipFieldName() {
        if (isMultiField())
            throw new IllegalCallerException("Column '" + getFullFieldName() + "' is a multi-field column.");
        if (!hasRelationship())
            throw new IllegalCallerException("Column '" + getFullFieldName() + "' has no relationship.");
        return getFieldNameList().get(0);
    }

    public List<Column> getSubColumnList() {
        if (!isMultiField())
            throw new IllegalCallerException("Column '" + getFullFieldName() + "' is not a multi-field column.");

        List<Column> subColumnList = new ArrayList<>();
        for (String fieldData : data.split("\\+")) {
            Column subColumn = new Column();
            subColumn.setData(fieldData.trim());
            subColumn.setSearchable(searchable);
            subColumn.setSearch(search);
            subColumnList.add(subColumn);
        }
        return subColumnList;
    }

    public String getLeftJoinClause(String leftTableAlias, String rightTableAlias) {
        StringBuilder stringBuilder = new StringBuilder();
        String fieldName = getRelationshipFieldName();
        stringBuilder.append(" Left Join ");
        stringBuilder.append(leftTableAlias);
        stringBuilder.append(".");
        stringBuilder.append(fieldName);
        stringBuilder.append(" ");
        stringBuilder.append(rightTableAlias);
        return stringBuilder.toString();
    }

    private List<String> getFieldNameList() {
        List<String> fieldNameList = new ArrayList<>();
        Pattern fieldPattern = Pattern.compile("\\w+");
        Matcher matcher = fieldPattern.matcher(data);
        while (matcher.find() && !isMultiField()) {
            fieldNameList.add(matcher.group());
        }
        return fieldNameList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Column))
            return false;
        Column column = (Column) o;
        return searchable == column.searchable && Objects.equals(data, column.data) && Objects.equals(search, column.search);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, searchable, search);
    }
}
