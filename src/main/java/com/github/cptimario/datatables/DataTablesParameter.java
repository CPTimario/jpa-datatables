package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.Order;
import com.github.cptimario.datatables.components.Search;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class DataTablesParameter {
    private int draw;
    private int start;
    private int length;
    private List<Order> order;
    private List<Column> columns;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Search search;

    public DataTablesParameter() {
        setDraw(1);
        setStart(0);
        setLength(10);
        setSearch(new Search(""));
        setColumns(new ArrayList<>());
        setOrder(new ArrayList<>());
    }

    public String getSearchValue() {
        return search.getValue();
    }

    public void setSearchValue(String searchValue) {
        search.setValue(searchValue);
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
