package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.Search;
import com.github.cptimario.datatables.entity.InvalidEntity;
import com.github.cptimario.datatables.entity.ParentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataTablesTest {
    private Column id;
    private Column data;
    private Column childEntity;
    private Column otherEntity;
    private Column childEntityDate;
    private StringBuilder stringBuilder;
    private DataTablesParameter dataTablesParameter;
    private DataTables<ParentEntity> dataTables;

    @BeforeEach
    void setup() {
        List<Column> columnList = getColumnList();
        dataTablesParameter = new DataTablesParameter();
        dataTablesParameter.setColumns(columnList);
        dataTablesParameter.setSearchValue("123");
        dataTables = DataTables.of(ParentEntity.class, dataTablesParameter);
    }

    private List<Column> getColumnList() {
        id = new Column("id");
        data = new Column("data", "lorem ipsum");
        childEntity = new Column("childEntity.firstData", "lorem");
        otherEntity = new Column("otherEntity.firstData ~ otherEntity.secondData", "ipsum");
        childEntityDate = new Column("childEntity.date");
        return List.of(id, data, childEntity, otherEntity, childEntityDate);
    }

    @Test
    void initializationTestInvalidEntity() {
        String message = InvalidEntity.class.getName() + " is not a valid entity.";
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> DataTables.of(InvalidEntity.class, dataTablesParameter));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void getLeftJoinClauseTestMultipleColumn() {
        stringBuilder = new StringBuilder();
        stringBuilder.append("Left Join ");
        stringBuilder.append("parentEntity.childEntity ");
        stringBuilder.append("child_1 ");
        stringBuilder.append("Left Join ");
        stringBuilder.append("parentEntity.otherEntity ");
        stringBuilder.append("other_2");
        assertEquals(stringBuilder.toString(), dataTables.getLeftJoinClause());
    }

    @Test
    void getFromClauseTestResultListQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ParentEntity ");
        stringBuilder.append("parentEntity ");
        stringBuilder.append("Left Join ");
        stringBuilder.append("parentEntity.childEntity ");
        stringBuilder.append("child_1 ");
        stringBuilder.append("Left Join ");
        stringBuilder.append("parentEntity.otherEntity ");
        stringBuilder.append("other_2");
        assertEquals(stringBuilder.toString(), dataTables.getFromClause(DataTables.QueryType.RESULT_LIST));
    }

    @Test
    void getFromClauseTestFilteredCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ParentEntity ");
        stringBuilder.append("parentEntity ");
        stringBuilder.append("Left Join ");
        stringBuilder.append("parentEntity.childEntity ");
        stringBuilder.append("child_1 ");
        stringBuilder.append("Left Join ");
        stringBuilder.append("parentEntity.otherEntity ");
        stringBuilder.append("other_2");
        assertEquals(stringBuilder.toString(), dataTables.getFromClause(DataTables.QueryType.FILTERED_COUNT));
    }

    @Test
    void getFromClauseTestTotalCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ParentEntity ");
        stringBuilder.append("parentEntity");
        assertEquals(stringBuilder.toString(), dataTables.getFromClause(DataTables.QueryType.TOTAL_COUNT));
    }

    @Test
    void getSearchStringTest() {
        String globalSearch = dataTablesParameter.getSearchValue();
        assertEquals(globalSearch, dataTables.getSearchString(id));
        assertEquals(data.getSearchValue(), dataTables.getSearchString(data));
        assertEquals(childEntity.getSearchValue(), dataTables.getSearchString(childEntity));
        assertEquals(otherEntity.getSearchValue(), dataTables.getSearchString(otherEntity));
        assertEquals(globalSearch, dataTables.getSearchString(childEntityDate));
    }

    @Test
    void getColumnAliasTest() {
        assertEquals("parentEntity", dataTables.getColumnAlias(id));
        assertEquals("parentEntity", dataTables.getColumnAlias(data));
        assertEquals("child_1", dataTables.getColumnAlias(childEntity));
        assertEquals("child_1", dataTables.getColumnAlias(childEntityDate));
    }

    @Test
    void getColumnAliasTestMultiField() {
        String message = "Column '" + otherEntity.getData() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> dataTables.getColumnAlias(otherEntity));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void getQueryFieldNameTest() {
        assertEquals("parentEntity.id", dataTables.getQueryFieldName(id));
        assertEquals("parentEntity.data", dataTables.getQueryFieldName(data));
        assertEquals("child_1.firstData", dataTables.getQueryFieldName(childEntity));
        assertEquals("Concat(other_2.firstData, ' ~ ', other_2.secondData)", dataTables.getQueryFieldName(otherEntity));
        assertEquals("child_1.date", dataTables.getQueryFieldName(childEntityDate));
    }

    @Test
    void getQueryFieldNameTestFormattedColumn() {
        String formattedFieldName = "function('date_format', child_1.date, '%Y/%m/%d')";
        dataTablesParameter.setDateColumnFormat("%Y/%m/%d", 4);
        assertEquals("parentEntity.id", dataTables.getQueryFieldName(id));
        assertEquals("parentEntity.data", dataTables.getQueryFieldName(data));
        assertEquals("child_1.firstData", dataTables.getQueryFieldName(childEntity));
        assertEquals("Concat(other_2.firstData, ' ~ ', other_2.secondData)", dataTables.getQueryFieldName(otherEntity));
        assertEquals(formattedFieldName, dataTables.getQueryFieldName(childEntityDate, true));
    }

    @Test
    void getFieldQueryTest() {
        String fieldName = "parentEntity.id";
        String namedParameter = "id_0";
        stringBuilder = new StringBuilder();
        stringBuilder.append("Upper(");
        stringBuilder.append(fieldName);
        stringBuilder.append(") ");
        stringBuilder.append("Like Upper(Concat('%', :");
        stringBuilder.append(namedParameter);
        stringBuilder.append(", '%')) Escape '#'");
        assertEquals(stringBuilder.toString(), dataTables.getFieldQuery(fieldName, namedParameter));
    }

    @Test
    void getSearchConditionTest() {
        List<String> searchConditionList = new ArrayList<>();
        searchConditionList.add(dataTables.getFieldQuery("parentEntity.id", "value_0"));
        searchConditionList.add(dataTables.getFieldQuery("parentEntity.data", "value_1"));
        searchConditionList.add(dataTables.getFieldQuery("child_1.firstData", "value_2"));
        searchConditionList.add(dataTables.getFieldQuery("Concat(other_2.firstData, ' ~ ', other_2.secondData)", "value_3"));
        searchConditionList.add(dataTables.getFieldQuery("child_1.date", "value_4"));
        String searchCondition = "(" + String.join(" Or ", searchConditionList) + ")";
        assertEquals(searchCondition, dataTables.getSearchCondition(new QueryParameter()));
    }

    @Test
    void getQueryTestTotalCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append("Select Count(parentEntity)");
        stringBuilder.append(dataTables.getFromClause(DataTables.QueryType.TOTAL_COUNT));
        assertEquals(stringBuilder.toString(), dataTables.getQuery(new QueryParameter(), DataTables.QueryType.TOTAL_COUNT));
    }

    @Test
    void getQueryTestFilteredCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append("Select Count(parentEntity)");
        stringBuilder.append(dataTables.getFromClause(DataTables.QueryType.FILTERED_COUNT));
        stringBuilder.append(" Where ");
        stringBuilder.append(dataTables.getSearchCondition(new QueryParameter()));
        assertEquals(stringBuilder.toString(), dataTables.getQuery(new QueryParameter(), DataTables.QueryType.FILTERED_COUNT));
    }

    @Test
    void getQueryTestResultListQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append("Select parentEntity");
        stringBuilder.append(dataTables.getFromClause(DataTables.QueryType.RESULT_LIST));
        stringBuilder.append(" Where ");
        stringBuilder.append(dataTables.getSearchCondition(new QueryParameter()));
        assertEquals(stringBuilder.toString(), dataTables.getQuery(new QueryParameter(), DataTables.QueryType.RESULT_LIST));
    }

    @Test
    void escapeWildcardsTest() {
        String text = "asd%%asd% asd__as#d _ asdasd% asd";
        String expected = "asd#%#%asd#% asd#_#_as##d #_ asdasd#% asd";
        assertEquals(expected, dataTables.escapeWildcards(text));
    }
}
