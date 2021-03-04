package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;
import com.github.cptimario.datatables.components.QueryType;
import com.github.cptimario.datatables.components.Search;
import com.github.cptimario.datatables.entity.ParentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataTablesTest {
    private Column id;
    private Column data;
    private Column firstSubEntity;
    private Column secondSubEntity;
    private Column firstSubEntityDate;
    private List<Column> columnList;
    private StringBuilder stringBuilder;
    private DataTablesParameter crossJoinParameter;
    private DataTablesParameter leftJoinParameter;
    private DataTables<ParentEntity> leftJoinDataTables;
    private DataTables<ParentEntity> crossJoinDataTables;

    @BeforeEach
    void setup() {
        columnList = getColumnList();

        crossJoinParameter = new DataTablesParameter();
        crossJoinParameter.setColumns(columnList);
        crossJoinParameter.setSearch(new Search("123"));
        crossJoinDataTables = DataTables.of(ParentEntity.class, crossJoinParameter);

        leftJoinParameter = new DataTablesParameter();
        leftJoinParameter.setColumns(columnList);
        leftJoinParameter.setSearch(new Search("123"));
        leftJoinDataTables = DataTables.of(ParentEntity.class, leftJoinParameter, JoinType.LEFT_JOIN);
    }

    private List<Column> getColumnList() {
        id = new Column();
        data = new Column();
        firstSubEntity = new Column();
        secondSubEntity = new Column();
        firstSubEntityDate = new Column();

        id.setData("id");
        id.setSearchable(true);

        data.setData("data");
        data.setSearch(new Search("lorem ipsum"));
        data.setSearchable(true);

        firstSubEntity.setData("firstSubEntity.firstData");
        firstSubEntity.setSearch(new Search("lorem"));
        firstSubEntity.setSearchable(true);

        secondSubEntity.setData("secondSubEntity?.firstData + secondSubEntity?.secondData");
        secondSubEntity.setSearch(new Search("ipsum"));
        secondSubEntity.setSearchable(true);

        firstSubEntityDate.setData("firstSubEntity.date");
        firstSubEntityDate.setSearchable(true);

        return List.of(id, data, firstSubEntity, secondSubEntity, firstSubEntityDate);
    }

    @Test
    void getLeftJoinClauseTestMultipleColumn() {
        Column firstSubColumn = secondSubEntity.getSubColumnList().get(0);
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Left Join ");
        stringBuilder.append("parentEntity.");
        stringBuilder.append(firstSubEntity.getEntityField());
        stringBuilder.append(" first_1 ");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("parentEntity.");
        stringBuilder.append(firstSubColumn.getEntityField());
        stringBuilder.append(" secon_2");
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getLeftJoinClause(columnList));
    }

    @Test
    void getFromClauseTestDefault() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ParentEntity");
        stringBuilder.append(" parentEntity");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFromClause());
    }

    @Test
    void getFromClauseTestCrossJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ParentEntity");
        stringBuilder.append(" parentEntity");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFromClause());
    }

    @Test
    void getFromClauseTestLeftJoin() {
        Column firstSubColumn = secondSubEntity.getSubColumnList().get(0);
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ParentEntity");
        stringBuilder.append(" parentEntity");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("parentEntity.");
        stringBuilder.append(firstSubEntity.getEntityField());
        stringBuilder.append(" first_1 ");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("parentEntity.");
        stringBuilder.append(firstSubColumn.getEntityField());
        stringBuilder.append(" secon_2");
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getFromClause());
    }

    @Test
    void getSearchStringTest() {
        String globalSearch = crossJoinParameter.getSearchValue();
        assertEquals(globalSearch, crossJoinDataTables.getSearchString(id));
        assertEquals(data.getSearchValue(), crossJoinDataTables.getSearchString(data));
        assertEquals(firstSubEntity.getSearchValue(), crossJoinDataTables.getSearchString(firstSubEntity));
        assertEquals(secondSubEntity.getSearchValue(), crossJoinDataTables.getSearchString(secondSubEntity));
        assertEquals(globalSearch, crossJoinDataTables.getSearchString(firstSubEntityDate));
    }

    @Test
    void getColumnAliasTest() {
        assertEquals("parentEntity", crossJoinDataTables.getColumnAlias(id));
        assertEquals("parentEntity", crossJoinDataTables.getColumnAlias(data));
        assertEquals("parentEntity", crossJoinDataTables.getColumnAlias(firstSubEntity));
        assertEquals("parentEntity", crossJoinDataTables.getColumnAlias(secondSubEntity));
        assertEquals("parentEntity", crossJoinDataTables.getColumnAlias(firstSubEntityDate));

        assertEquals("parentEntity", leftJoinDataTables.getColumnAlias(id));
        assertEquals("parentEntity", leftJoinDataTables.getColumnAlias(data));
        assertEquals("first_1", leftJoinDataTables.getColumnAlias(firstSubEntity));
        assertEquals("first_1", leftJoinDataTables.getColumnAlias(firstSubEntityDate));
        assertThrows(IllegalCallerException.class, () -> leftJoinDataTables.getColumnAlias(secondSubEntity));
    }

    @Test
    void getQueryFieldNameTestCrossJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("parentEntity.secondSubEntity.firstData, ' ',");
        stringBuilder.append("parentEntity.secondSubEntity.secondData ) ");
        assertEquals("parentEntity.id", crossJoinDataTables.getQueryFieldName(id));
        assertEquals("parentEntity.data", crossJoinDataTables.getQueryFieldName(data));
        assertEquals("parentEntity.firstSubEntity.firstData", crossJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQueryFieldName(secondSubEntity));
        assertEquals("parentEntity.firstSubEntity.date", crossJoinDataTables.getQueryFieldName(firstSubEntityDate));
    }

    @Test
    void getQueryFieldNameTestLeftJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        assertEquals("parentEntity.id", leftJoinDataTables.getQueryFieldName(id));
        assertEquals("parentEntity.data", leftJoinDataTables.getQueryFieldName(data));
        assertEquals("first_1.firstData", leftJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQueryFieldName(secondSubEntity));
        assertEquals("first_1.date", leftJoinDataTables.getQueryFieldName(firstSubEntityDate));
    }

    @Test
    void getQueryFieldNameTestFormattedColumn() {
        String formattedFieldName = "function('date_format', first_1.date, '%Y/%m/%d')";
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        leftJoinParameter.setDateColumnFormat("%Y/%m/%d", 4);
        assertEquals("parentEntity.id", leftJoinDataTables.getQueryFieldName(id));
        assertEquals("parentEntity.data", leftJoinDataTables.getQueryFieldName(data));
        assertEquals("first_1.firstData", leftJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQueryFieldName(secondSubEntity));
        assertEquals(formattedFieldName, leftJoinDataTables.getQueryFieldName(firstSubEntityDate, true));
    }

    @Test
    void getFieldQueryTest() {
        String fieldName = "parentEntity.id";
        String namedParameter = "id_0";
        stringBuilder = new StringBuilder();
        stringBuilder.append(fieldName);
        stringBuilder.append(" LIKE CONCAT('%', :");
        stringBuilder.append(namedParameter);
        stringBuilder.append(", '%') ESCAPE '#' ");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFieldQuery(fieldName, namedParameter));
    }

    @Test
    void getSearchConditionTestCrossJoin() {
        List<String> searchConditionList = new ArrayList<>();
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("parentEntity.secondSubEntity.firstData, ' ',");
        stringBuilder.append("parentEntity.secondSubEntity.secondData ) ");
        searchConditionList.add(crossJoinDataTables.getFieldQuery("parentEntity.id", "value_0"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("parentEntity.data", "value_1"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("parentEntity.firstSubEntity.firstData", "value_2"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery(stringBuilder.toString(), "value_3"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("parentEntity.firstSubEntity.date", "value_4"));
        String searchCondition = " ( " + String.join(" Or ", searchConditionList) + " ) ";
        assertEquals(searchCondition, crossJoinDataTables.getSearchCondition(new QueryParameter()));
    }

    @Test
    void getSearchConditionTestLeftJoin() {
        List<String> searchConditionList = new ArrayList<>();
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        searchConditionList.add(leftJoinDataTables.getFieldQuery("parentEntity.id", "value_0"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("parentEntity.data", "value_1"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("first_1.firstData", "value_2"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery(stringBuilder.toString(), "value_3"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("first_1.date", "value_4"));
        String searchCondition = " ( " + String.join(" Or ", searchConditionList) + " ) ";
        assertEquals(searchCondition, leftJoinDataTables.getSearchCondition(new QueryParameter()));
    }

    @Test
    void getQueryTestCrossJoinTotalCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select Count(*) ");
        stringBuilder.append(crossJoinDataTables.getFromClause());
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(new QueryParameter(), QueryType.TOTAL_COUNT));
    }

    @Test
    void getQueryTestCrossJoinFilteredCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select Count(*) ");
        stringBuilder.append(crossJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(crossJoinDataTables.getSearchCondition(new QueryParameter()));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(new QueryParameter(), QueryType.FILTERED_COUNT));
    }

    @Test
    void getQueryTestCrossJoinResultListQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select parentEntity");
        stringBuilder.append(crossJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(crossJoinDataTables.getSearchCondition(new QueryParameter()));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(new QueryParameter(), QueryType.RESULT_LIST));
    }

    @Test
    void getQueryTestLeftJoinTotalCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select Count(*) ");
        stringBuilder.append(leftJoinDataTables.getFromClause());
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(new QueryParameter(), QueryType.TOTAL_COUNT));
    }

    @Test
    void getQueryTestLeftJoinFilteredCountQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select Count(*) ");
        stringBuilder.append(leftJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(leftJoinDataTables.getSearchCondition(new QueryParameter()));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(new QueryParameter(), QueryType.FILTERED_COUNT));
    }

    @Test
    void getQueryTestLeftJoinResultListQuery() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select parentEntity");
        stringBuilder.append(leftJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(leftJoinDataTables.getSearchCondition(new QueryParameter()));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(new QueryParameter(), QueryType.RESULT_LIST));
    }
}
