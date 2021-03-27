package io.github.cptimario.datatables;

import io.github.cptimario.datatables.components.Column;
import io.github.cptimario.datatables.components.Order;
import io.github.cptimario.datatables.components.Search;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DataTablesParameter class handles the storage and manipulation of the datatables parameters received from the client side
 *
 * @author Christopher Timario
 * @version v1.0.0
 */
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

    /**
     * Creates an instance of datatables parameters with default values
     */
    public DataTablesParameter() {
        setDraw(1);
        setStart(0);
        setLength(10);
        setSearch(new Search(""));
        setColumns(new ArrayList<>());
        setOrder(new ArrayList<>());
    }

    /**
     * Returns the value of the search parameter received from the client side
     *
     * @return the search text value
     */
    public String getSearchValue() {
        return search.getValue();
    }

    /**
     * Sets the value of the search parameter
     *
     * @param searchValue the value to set to the search parameter
     */
    public void setSearchValue(String searchValue) {
        search.setValue(searchValue);
    }

    /**
     * Sets the sql date format for the column.
     *
     * @param format        sql date format
     * @param columnIndexes indexes of the columns to be formatted
     * @apiNote This method will format the columns which contains dates so that they can be searched properly
     * @implSpec The database used must support the 'date_format'
     * @implNote This implementation will use the 'date_format' function from the database.
     * If the database used does not support this function, it will throw an exception.
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
