package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;
import com.github.cptimario.datatables.components.Search;
import com.github.cptimario.datatables.entity.ValidEntity;
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
    private QueryParameter queryParameter;
    private DataTablesParameter crossJoinParameter;
    private DataTablesParameter leftJoinParameter;
    private DataTables<ValidEntity> leftJoinDataTables;
    private DataTables<ValidEntity> crossJoinDataTables;

    @BeforeEach
    void setup() {
        columnList = getColumnList();
        queryParameter = new QueryParameter();

        crossJoinParameter = new DataTablesParameter();
        crossJoinParameter.setColumns(columnList);
        crossJoinParameter.setSearch(new Search("123"));
        crossJoinDataTables = DataTables.of(ValidEntity.class, crossJoinParameter);

        leftJoinParameter = new DataTablesParameter();
        leftJoinParameter.setColumns(columnList);
        leftJoinParameter.setSearch(new Search("123"));
        leftJoinDataTables = DataTables.of(ValidEntity.class, leftJoinParameter, JoinType.LEFT_JOIN);
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
        stringBuilder.append("validentity.");
        stringBuilder.append(firstSubEntity.getRelationshipFieldName());
        stringBuilder.append(" first_1 ");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("validentity.");
        stringBuilder.append(firstSubColumn.getRelationshipFieldName());
        stringBuilder.append(" secon_2");
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getLeftJoinClause(columnList));
    }

    @Test
    void getFromClauseTestDefault() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ValidEntity");
        stringBuilder.append(" validentity");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFromClause());
    }

    @Test
    void getFromClauseTestCrossJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ValidEntity");
        stringBuilder.append(" validentity");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFromClause());
    }

    @Test
    void getFromClauseTestLeftJoin() {
        Column firstSubColumn = secondSubEntity.getSubColumnList().get(0);
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ValidEntity");
        stringBuilder.append(" validentity");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("validentity.");
        stringBuilder.append(firstSubEntity.getRelationshipFieldName());
        stringBuilder.append(" first_1 ");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("validentity.");
        stringBuilder.append(firstSubColumn.getRelationshipFieldName());
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
        assertEquals("validentity", crossJoinDataTables.getColumnAlias(id));
        assertEquals("validentity", crossJoinDataTables.getColumnAlias(data));
        assertEquals("validentity", crossJoinDataTables.getColumnAlias(firstSubEntity));
        assertEquals("validentity", crossJoinDataTables.getColumnAlias(secondSubEntity));
        assertEquals("validentity", crossJoinDataTables.getColumnAlias(firstSubEntityDate));

        assertEquals("validentity", leftJoinDataTables.getColumnAlias(id));
        assertEquals("validentity", leftJoinDataTables.getColumnAlias(data));
        assertEquals("first_1", leftJoinDataTables.getColumnAlias(firstSubEntity));
        assertEquals("first_1", leftJoinDataTables.getColumnAlias(firstSubEntityDate));
        assertThrows(IllegalCallerException.class, () -> leftJoinDataTables.getColumnAlias(secondSubEntity));
    }

    @Test
    void getQueryFieldNameTestCrossJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("validentity.secondSubEntity.firstData, ' ',");
        stringBuilder.append("validentity.secondSubEntity.secondData ) ");
        assertEquals("validentity.id", crossJoinDataTables.getQueryFieldName(id));
        assertEquals("validentity.data", crossJoinDataTables.getQueryFieldName(data));
        assertEquals("validentity.firstSubEntity.firstData", crossJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQueryFieldName(secondSubEntity));
        assertEquals("validentity.firstSubEntity.date", crossJoinDataTables.getQueryFieldName(firstSubEntityDate));
    }

    @Test
    void getQueryFieldNameTestLeftJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        assertEquals("validentity.id", leftJoinDataTables.getQueryFieldName(id));
        assertEquals("validentity.data", leftJoinDataTables.getQueryFieldName(data));
        assertEquals("first_1.firstData", leftJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQueryFieldName(secondSubEntity));
        assertEquals("first_1.date", leftJoinDataTables.getQueryFieldName(firstSubEntityDate));
    }

    @Test
    void getQueryFieldNameTestFormattedColumn() {
        String formattedFieldName = "Format(first_1.date, 'yyyy/MM/dd')";
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        leftJoinParameter.setColumnFormat("yyyy/MM/dd", 4);
        assertEquals("validentity.id", leftJoinDataTables.getQueryFieldName(id));
        assertEquals("validentity.data", leftJoinDataTables.getQueryFieldName(data));
        assertEquals("first_1.firstData", leftJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQueryFieldName(secondSubEntity));
        assertEquals(formattedFieldName, leftJoinDataTables.getQueryFieldName(firstSubEntityDate));
    }

    @Test
    void getFieldQueryTest() {
        String fieldName = "validentity.id";
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
        stringBuilder.append("validentity.secondSubEntity.firstData, ' ',");
        stringBuilder.append("validentity.secondSubEntity.secondData ) ");
        searchConditionList.add(crossJoinDataTables.getFieldQuery("validentity.id", "value_0"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("validentity.data", "value_1"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("validentity.firstSubEntity.firstData", "value_2"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery(stringBuilder.toString(), "value_3"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("validentity.firstSubEntity.date", "value_4"));
        String searchCondition = " ( " + String.join(" Or ", searchConditionList) + " ) ";
        assertEquals(searchCondition, crossJoinDataTables.getSearchCondition(queryParameter));
    }

    @Test
    void getSearchConditionTestLeftJoin() {
        List<String> searchConditionList = new ArrayList<>();
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        searchConditionList.add(leftJoinDataTables.getFieldQuery("validentity.id", "value_0"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("validentity.data", "value_1"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("first_1.firstData", "value_2"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery(stringBuilder.toString(), "value_3"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("first_1.date", "value_4"));
        String searchCondition = " ( " + String.join(" Or ", searchConditionList) + " ) ";
        assertEquals(searchCondition, leftJoinDataTables.getSearchCondition(queryParameter));
    }

    @Test
    void getQueryTestCrossJoinAndIsSearchFalse() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select validentity");
        stringBuilder.append(crossJoinDataTables.getFromClause());
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(queryParameter, false));
    }

    @Test
    void getQueryTestCrossJoinAndIsSearchTrue() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select validentity");
        stringBuilder.append(crossJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(crossJoinDataTables.getSearchCondition(queryParameter));
        System.out.println(crossJoinDataTables.getWhereClause(queryParameter, true));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(queryParameter, true));
    }

    @Test
    void getQueryTestLeftJoinAndIsSearchFalse() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select validentity");
        stringBuilder.append(leftJoinDataTables.getFromClause());
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(queryParameter, false));
    }

    @Test
    void getQueryTestLeftJoinAndIsSearchTrue() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Select validentity");
        stringBuilder.append(leftJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(leftJoinDataTables.getSearchCondition(queryParameter));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(queryParameter, true));
    }
}
