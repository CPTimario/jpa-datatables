package com.github.cptimario.datatables.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Column {
    private String data;
    private boolean searchable;
    private boolean orderable;
    private Search search;

    @JsonIgnore
    private String format;

    @JsonIgnore
    @ToString.Exclude
    private List<Column> subColumnList;

    public Column() {
        this("", "");
    }

    public Column(String data) {
        this(data, "");
    }

    public Column(String data, String searchText) {
        setSearchable(true);
        setOrderable(true);
        setSearch(new Search(searchText));
        setData(data);
    }

    public void setData(String data) {
        this.data = data;
        if (isMultiField()) {
            initializeSubColumnList();
        }
    }

    private void initializeSubColumnList() {
        subColumnList = new ArrayList<>();
        for (String fieldData : getFieldList()) {
            Column subColumn = new Column();
            subColumn.setData(fieldData.trim());
            subColumn.setSearchable(searchable);
            subColumn.setOrderable(orderable);
            subColumn.setSearch(search);
            subColumnList.add(subColumn);
        }
    }

    /**
     * Returns the search value for {@code this} column.
     *
     * @return the search value
     */
    public String getSearchValue() {
        return search.getValue();
    }

    /**
     * Checks whether {@code this} column contains multiple fields.
     * DataTables columns can be composed of multiple entity fields.
     *
     * @return {@code true} if a multi-field column, otherwise {@code false}.
     */
    public boolean isMultiField() {
        List<String> fieldList = getFieldList();
        return fieldList.size() > 1;
    }

    /**
     * Returns the field delimiter for multi-field columns.
     *
     * @return the delimiter if a multi-field column. Otherwise, an empty string.
     */
    public String getFieldDelimiter() {
        String regex = "\\w+(?:\\.\\w+)*(?<delimiter>\\W+)(?:\\w+(?:\\.\\w+)*)+";
        Matcher fieldDelimiter = getMatcher(regex, data);
        return fieldDelimiter.matches() ? fieldDelimiter.group("delimiter") : "";
    }

    /**
     * Checks whether the field on {@code this} column contains a relationship to another entity.
     *
     * @return {@code true} if field has relationship, otherwise {@code false}.
     * @throws IllegalCallerException if {@code this} column is multi-field.
     */
    public boolean hasRelationship() {
        if (isMultiField()) {
            throw new IllegalCallerException("Column '" + getData() + "' is a multi-field column.");
        }
        String regex = "\\w+(?:\\.\\w+)+";
        Matcher relationship = getMatcher(regex, data);
        return Objects.nonNull(data) && relationship.matches();
    }

    /**
     * Returns a list of fields of {@code this} column.
     *
     * @return list of fields
     */
    public List<String> getFieldList() {
        List<String> fieldList = new ArrayList<>();
        String regex = "\\w+(?:\\.\\w+)*";
        Matcher field = getMatcher(regex, data);
        while (field.find()) {
            fieldList.add(field.group());
        }
        return fieldList;
    }

    /**
     * Returns the base field of {@code this} column.
     *
     * @return base field
     * @throws IllegalCallerException if {@code this} column is multi-field
     *                                or if {@code this} column does not contain relationship to another entity.
     */
    public String getBaseField() {
        if (!hasRelationship()) {
            throw new IllegalCallerException("Column '" + getData() + "' has no relationship.");
        }
        String regex = "(?<entityField>\\w+)(?:\\.\\w+)+";
        Matcher entityField = getMatcher(regex, data);
        return entityField.matches() ? entityField.group("entityField") : "";
    }

    private Matcher getMatcher(String regex, String input) {
        Pattern patern = Pattern.compile(regex);
        return patern.matcher(input);
    }
}
