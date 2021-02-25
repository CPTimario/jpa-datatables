import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColumnTest {
    private Column singleFieldColumn;
    private Column nullableColumn;
    private Column hasRelationshipColumn;
    private Column multiFieldColumn;

    @BeforeEach
    public void setup() {
        singleFieldColumn = new Column();
        nullableColumn = new Column();
        hasRelationshipColumn = new Column();
        multiFieldColumn = new Column();

        singleFieldColumn.setData("field");

        nullableColumn.setData("entity?.field");

        hasRelationshipColumn.setData("entity.field");

        multiFieldColumn.setData("firstField + secondField");
    }

    @Test
    public void isMultiFieldTest() {
        assertTrue(multiFieldColumn.isMultiField());
        assertFalse(singleFieldColumn.isMultiField());
        assertFalse(nullableColumn.isMultiField());
        assertFalse(hasRelationshipColumn.isMultiField());
    }

    @Test
    public void isNullableTest() {
        assertTrue(nullableColumn.isNullable());
        assertFalse(singleFieldColumn.isNullable());
        assertFalse(hasRelationshipColumn.isNullable());
    }

    @Test
    public void hasRelationship() {
        assertTrue(hasRelationshipColumn.hasRelationship());
        assertTrue(nullableColumn.hasRelationship());
        assertFalse(singleFieldColumn.hasRelationship());
    }

    @Test
    public void getFullFieldNameTest() {
        assertEquals("field", singleFieldColumn.getFullFieldName());
        assertEquals("entity.field", nullableColumn.getFullFieldName());
        assertEquals("entity.field", hasRelationshipColumn.getFullFieldName());
        assertEquals("", multiFieldColumn.getFullFieldName());
    }

    @Test
    public void getRelationshipFieldNameTest() {
        assertEquals("", singleFieldColumn.getRelationshipFieldName());
        assertEquals("entity", nullableColumn.getRelationshipFieldName());
        assertEquals("entity", hasRelationshipColumn.getRelationshipFieldName());
        assertEquals("", multiFieldColumn.getRelationshipFieldName());
    }

    @Test
    public void getSubColumnListTest() {
        Column firstSubColumn = new Column();
        Column secondSubColumn = new Column();

        firstSubColumn.setData("firstField");
        firstSubColumn.setSearchable(multiFieldColumn.isSearchable());
        firstSubColumn.setSearch(multiFieldColumn.getSearch());

        secondSubColumn.setData("secondField");
        secondSubColumn.setSearchable(multiFieldColumn.isSearchable());
        secondSubColumn.setSearch(multiFieldColumn.getSearch());

        assertIterableEquals(List.of(firstSubColumn, secondSubColumn), multiFieldColumn.getSubColumnList());
    }

    @Test
    public void getLeftJoinClauseTest() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Left Join ");
        stringBuilder.append("left.");
        stringBuilder.append(hasRelationshipColumn.getRelationshipFieldName());
        stringBuilder.append(" right");
        assertEquals(stringBuilder.toString(), hasRelationshipColumn.getLeftJoinClause("left", "right"));
        assertEquals("", multiFieldColumn.getLeftJoinClause("left", "right"));
    }
}
