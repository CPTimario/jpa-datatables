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
        this.orderable = true;
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

    /**
     * Checks whether {@code this} column contains multiple fields.
     * DataTables columns can be composed of multiple entity fields.
     *
     * @return {@code true} if a multi-field column, otherwise {@code false}.
     */
    public boolean isMultiField() {
        String regex = "\\w+\\??(?:\\.\\w+\\??)*(?:[^\\.\\d\\w]+\\w+\\??(?:\\.\\w+\\??)*)+";
        Matcher multiField = getMatcher(regex, data);
        return Objects.nonNull(data) && multiField.matches();
    }

    /**
     * Checks whether the field on {@code this} column contains a relationship to another entity.
     *
     * @return {@code true} if field has relationship, otherwise {@code false}.
     * @throws IllegalCallerException if {@code this} column is multi-field.
     */
    public boolean hasRelationship() {
        if (isMultiField())
            throw new IllegalCallerException("Column '" + getField() + "' is a multi-field column.");
        String regex = "\\w+\\??(?:\\.\\w+\\??)+";
        Matcher relationship = getMatcher(regex, data);
        return Objects.nonNull(data) && relationship.matches();
    }

    /**
     * Checks whether the field on {@code this} column is nullable.
     *
     * @return {@code true} if field is nullable, otherwise {@code false}.
     * @throws IllegalCallerException if {@code this} column is multi-field.
     */
    public boolean isNullable() {
        if (isMultiField())
            throw new IllegalCallerException("Column '" + getField() + "' is a multi-field column.");
        String regex = "\\w+\\?(?:\\.\\w+\\??)*";
        Matcher nullable = getMatcher(regex, data);
        return Objects.nonNull(data) && nullable.matches();
    }

    /**
     * Returns the field of {@code this} column.
     * For multi-field columns, the fields are delimited by whitespace.
     * The nullable symbol {@code ?} is also removed.
     *
     * @return field
     */
    public String getField() {
        return String.join(" ", getFieldList());
    }

    /**
     * Returns a list of fields of {@code this} column.
     * The nullable symbol {@code ?} is also removed.
     *
     * @return list of fields
     */
    public List<String> getFieldList() {
        List<String> fieldList = new ArrayList<>();
        String regex = "\\w+\\??(?:\\.\\w+\\??)*";
        Matcher field = getMatcher(regex, data);
        while (field.find()) {
            fieldList.add(field.group().replace("?", ""));
        }
        return fieldList;
    }

    /**
     * Returns the entity field of {@code this} column.
     *
     * @return entity field
     * @throws IllegalCallerException if {@code this} column is multi-field
     *                                or if {@code this} column does not contain relationship to another entity.
     */
    public String getEntityField() {
        if (!hasRelationship())
            throw new IllegalCallerException("Column '" + getField() + "' has no relationship.");
        String regex = "(?<entityField>\\w+)\\??(?:\\.\\w+\\??)+";
        Matcher entityField = getMatcher(regex, data);
        entityField.find();
        return entityField.group("entityField");
    }

    /**
     * Returns the subfields of {@code this} column as list of {@link Column}.
     *
     * @return list of subfield columns
     * @throws IllegalCallerException if {@code this} column is not multi-field.
     */
    public List<Column> getSubColumnList() {
        if (!isMultiField())
            throw new IllegalCallerException("Column '" + getField() + "' is not a multi-field column.");
        List<Column> subColumnList = new ArrayList<>();
        for (String fieldData : getFieldList()) {
            Column subColumn = new Column();
            subColumn.setData(fieldData.trim());
            subColumn.setSearchable(searchable);
            subColumn.setOrderable(orderable);
            subColumn.setSearch(search);
            subColumnList.add(subColumn);
        }
        return subColumnList;
    }

    private Matcher getMatcher(String regex, String input) {
        Pattern patern = Pattern.compile(regex);
        return patern.matcher(input);
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
