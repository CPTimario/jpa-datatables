package io.github.cptimario.datatables;

import io.github.cptimario.datatables.components.Column;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class DataTablesParameterTest {
    @Test
    void setDateColumnFormatTest() {
        DataTablesParameter dataTablesParameter = new DataTablesParameter();
        List<Column> columnList = getColumnList();
        Column formattedColumn = new Column("data");
        formattedColumn.setFormat("format");
        dataTablesParameter.setColumns(columnList);
        dataTablesParameter.setDateColumnFormat("format", 0, 1, 4);
        columnList.set(0, formattedColumn);
        columnList.set(1, formattedColumn);
        List<Column> actual = dataTablesParameter.getColumns();
        assertIterableEquals(columnList, actual);
        assertIterableEquals(getSubColumnList(), actual.get(4).getSubColumnList());
    }

    private List<Column> getColumnList() {
        List<Column> columnList = new ArrayList<>();
        Column multiField = new Column("first second");
        columnList.add(new Column("data"));
        columnList.add(new Column("data"));
        columnList.add(new Column("data"));
        columnList.add(new Column("data"));
        columnList.add(multiField);
        return columnList;
    }

    private List<Column> getSubColumnList() {
        Column first = new Column("first");
        Column second = new Column("second");
        first.setFormat("format");
        second.setFormat("format");
        return List.of(first, second);
    }
}
