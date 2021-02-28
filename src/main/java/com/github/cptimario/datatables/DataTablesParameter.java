package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.Order;
import com.github.cptimario.datatables.components.Search;

import java.util.List;

public class DataTablesParameter {
    private int draw;
    private int start;
    private int length;
    private Search search;
    private List<Order> orderList;
    private List<Column> columnList;

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

    public List<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    public List<Column> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<Column> columnList) {
        this.columnList = columnList;
    }

    public String getSearchValue() {
        return search.getValue();
    }

    public void flagColumnsContainingDateByIndex(String delimiter, int... columnIndexes) {
        for (int index : columnIndexes) {
            Column column = columnList.get(index);
            column.setContainsDate(true);
            column.setDelimiter(delimiter);
        }
    }
}