package com.github.cptimario.datatables.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        singleFieldColumn.setName("field");
        singleFieldColumn.setData("field");

        nullableColumn.setName("nullableEntityField");
        nullableColumn.setData("entity?.field");

        hasRelationshipColumn.setName("entityField");
        hasRelationshipColumn.setData("entity.field");

        multiFieldColumn.setName("firstFieldSecondField");
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
        assertEquals("firstField secondField", multiFieldColumn.getFullFieldName());
    }
    
    @Test
    public void getFullFieldNameListTestNotMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getFullFieldNameList());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    public void getFullFieldNameListTest() {
        assertIterableEquals(List.of("firstField", "secondField"), multiFieldColumn.getFullFieldNameList());
    }

    @Test
    public void getRelationshipFieldNameTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getFullFieldName() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.getRelationshipFieldName());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    public void getRelationshipFieldNameTestNoRelationshipInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' has no relationship.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getRelationshipFieldName());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    public void getRelationshipFieldNameTest() {
        assertEquals("entity", hasRelationshipColumn.getRelationshipFieldName());
    }

    @Test
    public void getSubColumnListTestNotMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getSubColumnList());
        assertEquals(exception.getMessage(), message);
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
    public void getLeftJoinClauseTestMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getSubColumnList());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    public void getLeftJoinClauseTest() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Left Join ");
        stringBuilder.append("left.");
        stringBuilder.append(hasRelationshipColumn.getRelationshipFieldName());
        stringBuilder.append(" right");
        assertEquals(stringBuilder.toString(), hasRelationshipColumn.getLeftJoinClause("left", "right"));
    }
}
