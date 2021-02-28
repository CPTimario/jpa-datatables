package com.github.cptimario.datatables;

import com.github.cptimario.datatables.components.Column;
import com.github.cptimario.datatables.components.JoinType;
import com.github.cptimario.datatables.components.Search;
import com.github.cptimario.datatables.entity.InvalidEntity;
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
        crossJoinParameter.setColumnList(columnList);
        crossJoinParameter.setSearch(new Search("123"));
        crossJoinDataTables = DataTables.of(ValidEntity.class, crossJoinParameter);

        leftJoinParameter = new DataTablesParameter();
        leftJoinParameter.setColumnList(columnList);
        leftJoinParameter.setSearch(new Search("123"));
        leftJoinDataTables = DataTables.of(ValidEntity.class, leftJoinParameter, JoinType.LEFT_JOIN);
    }

    private List<Column> getColumnList() {
        id = new Column();
        data = new Column();
        firstSubEntity = new Column();
        secondSubEntity = new Column();

        id.setData("id");

        data.setData("data");
        data.setSearch(new Search("lorem ipsum"));

        firstSubEntity.setData("firstSubEntity.firstData");
        firstSubEntity.setSearch(new Search("lorem"));

        secondSubEntity.setData("secondSubEntity?.firstData + secondSubEntity?.secondData");
        secondSubEntity.setSearch(new Search("ipsum"));

        return List.of(id, data, firstSubEntity, secondSubEntity);
    }

    @Test
    void isEntityTest() {
        assertTrue(DataTables.isEntity(ValidEntity.class));
        assertFalse(DataTables.isEntity(InvalidEntity.class));
    }

    @Test
    void datatablesConstructionInvalidEntity() {
        assertThrows(IllegalArgumentException.class, () -> DataTables.of(InvalidEntity.class, leftJoinParameter));
    }

    @Test
    void datatablesConstructionInvalidDataTablesParameters() {
        assertThrows(NullPointerException.class, () -> DataTables.of(ValidEntity.class, null));
    }

    @Test
    void getLeftJoinClauseTestSingleColumn() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Left Join ");
        stringBuilder.append("valid_0.");
        stringBuilder.append(firstSubEntity.getRelationshipFieldName());
        stringBuilder.append(" first_1");
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getLeftJoinClause(firstSubEntity));
    }

    @Test
    void getLeftJoinClauseTestMultipleColumn() {
        Column firstSubColumn = secondSubEntity.getSubColumnList().get(0);
        stringBuilder = new StringBuilder();
        stringBuilder.append(" Left Join ");
        stringBuilder.append("valid_0.");
        stringBuilder.append(firstSubEntity.getRelationshipFieldName());
        stringBuilder.append(" first_1 ");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("valid_0.");
        stringBuilder.append(firstSubColumn.getRelationshipFieldName());
        stringBuilder.append(" secon_2");
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getLeftJoinClause(columnList));
    }

    @Test
    void getFromClauseTestDefault() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ValidEntity");
        stringBuilder.append(" valid_0");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFromClause());
    }

    @Test
    void getFromClauseTestCrossJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ValidEntity");
        stringBuilder.append(" valid_0");
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getFromClause());
    }

    @Test
    void getFromClauseTestLeftJoin() {
        Column firstSubColumn = secondSubEntity.getSubColumnList().get(0);
        stringBuilder = new StringBuilder();
        stringBuilder.append(" From ");
        stringBuilder.append("ValidEntity");
        stringBuilder.append(" valid_0");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("valid_0.");
        stringBuilder.append(firstSubEntity.getRelationshipFieldName());
        stringBuilder.append(" first_1 ");
        stringBuilder.append(" Left Join ");
        stringBuilder.append("valid_0.");
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
    }

    @Test
    void getColumnAliasTest() {
        assertEquals("valid_0", crossJoinDataTables.getColumnAlias(id));
        assertEquals("valid_0", crossJoinDataTables.getColumnAlias(data));
        assertEquals("valid_0", crossJoinDataTables.getColumnAlias(firstSubEntity));
        assertEquals("valid_0", crossJoinDataTables.getColumnAlias(secondSubEntity));

        assertEquals("valid_0", leftJoinDataTables.getColumnAlias(id));
        assertEquals("valid_0", leftJoinDataTables.getColumnAlias(data));
        assertEquals("first_1", leftJoinDataTables.getColumnAlias(firstSubEntity));
        assertThrows(IllegalCallerException.class, () -> leftJoinDataTables.getColumnAlias(secondSubEntity));
    }

    @Test
    void getQueryFieldNameTestCrossJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("valid_0.secondSubEntity.firstData, ' ',");
        stringBuilder.append("valid_0.secondSubEntity.secondData ) ");
        assertEquals("valid_0.id", crossJoinDataTables.getQueryFieldName(id));
        assertEquals("valid_0.data", crossJoinDataTables.getQueryFieldName(data));
        assertEquals("valid_0.firstSubEntity.firstData", crossJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQueryFieldName(secondSubEntity));
    }

    @Test
    void getQueryFieldNameTestLeftJoin() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(" CONCAT ( ");
        stringBuilder.append("secon_2.firstData, ' ',");
        stringBuilder.append("secon_2.secondData ) ");
        assertEquals("valid_0.id", leftJoinDataTables.getQueryFieldName(id));
        assertEquals("valid_0.data", leftJoinDataTables.getQueryFieldName(data));
        assertEquals("first_1.firstData", leftJoinDataTables.getQueryFieldName(firstSubEntity));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQueryFieldName(secondSubEntity));
    }

    @Test
    void getFieldQueryTest() {
        String fieldName = "valid_0.id";
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
        stringBuilder.append("valid_0.secondSubEntity.firstData, ' ',");
        stringBuilder.append("valid_0.secondSubEntity.secondData ) ");
        searchConditionList.add(crossJoinDataTables.getFieldQuery("valid_0.id", "value_0"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("valid_0.data", "value_1"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery("valid_0.firstSubEntity.firstData", "value_2"));
        searchConditionList.add(crossJoinDataTables.getFieldQuery(stringBuilder.toString(), "value_3"));
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
        searchConditionList.add(leftJoinDataTables.getFieldQuery("valid_0.id", "value_0"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("valid_0.data", "value_1"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery("first_1.firstData", "value_2"));
        searchConditionList.add(leftJoinDataTables.getFieldQuery(stringBuilder.toString(), "value_3"));
        String searchCondition = " ( " + String.join(" Or ", searchConditionList) + " ) ";
        assertEquals(searchCondition, leftJoinDataTables.getSearchCondition(queryParameter));
    }

    @Test
    void getQueryTestCrossJoinAndIsSearchFalse() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(crossJoinDataTables.getFromClause());
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(queryParameter, false));
    }

    @Test
    void getQueryTestCrossJoinAndIsSearchTrue() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(crossJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(crossJoinDataTables.getSearchCondition(queryParameter));
        assertEquals(stringBuilder.toString(), crossJoinDataTables.getQuery(queryParameter, true));
    }

    @Test
    void getQueryTestLeftJoinAndIsSearchFalse() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(leftJoinDataTables.getFromClause());
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(queryParameter, false));
    }

    @Test
    void getQueryTestLeftJoinAndIsSearchTrue() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(leftJoinDataTables.getFromClause());
        stringBuilder.append(" Where ");
        stringBuilder.append(leftJoinDataTables.getSearchCondition(queryParameter));
        assertEquals(stringBuilder.toString(), leftJoinDataTables.getQuery(queryParameter, true));
    }
}
