package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.Order;
import com.github.cptimario.datatables.components.Search;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataTablesParameter {
    private int draw;
    private int start;
    private int length;
    private Search search;
    private List<Order> order;
    private List<Column> columns;

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public List<Order> getOrder() {
        if (Objects.isNull(order))
            return new ArrayList<>();
        return order;
    }

    public void setOrder(List<Order> order) {
        this.order = order;
    }

    public List<Column> getColumns() {
        if (Objects.isNull(columns))
            return new ArrayList<>();
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public String getSearchValue() {
        return search.getValue();
    }

    /**
     * Sets the sql date format for the column.
     *
     * @implSpec The database must support the 'date_format' function.
     * @param format sql date format
     * @param columnIndexes indexes of the columns to be formatted
     */
    public void setDateColumnFormat(String format, int... columnIndexes) {
        List<Column> columnList = getColumns();
        for (int index : columnIndexes) {
            Column column = columnList.get(index);
            if (column.isMultiField()) {
                for (Column subColumn : column.getSubColumnList()) {
                    subColumn.setFormat(format);
                }
            } else {
                column.setFormat(format);
            }
        }
    }
}
