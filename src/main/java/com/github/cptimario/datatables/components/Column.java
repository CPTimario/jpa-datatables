package com.github.cptimario.datatables.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Column {
    private String data;
    private String name;
    private boolean searchable;
    private Search search;
    private String delimiter;
    private boolean containsDate;

    public Column() {
        this.data = "";
        this.name = "";
        this.searchable = true;
        this.search = new Search();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
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

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        if (!containsDate)
            throw new IllegalCallerException("Column '" + getFullFieldName() + "' does not contain date.");
        this.delimiter = delimiter;
    }

    public boolean isContainsDate() {
        return containsDate;
    }

    public void setContainsDate(boolean containsDate) {
        this.containsDate = containsDate;
    }

    public boolean isMultiField() {
        return data.contains("+");
    }

    public boolean hasRelationship() {
        return data.contains(".");
    }

    public boolean isNullable() {
        return data.contains("?");
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
        return searchable == column.searchable && Objects.equals(data, column.data) && Objects.equals(name, column.name) && Objects.equals(search, column.search);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, name, searchable, search);
    }
}
