package com.github.cptimario.datatables.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColumnTest {
    private Column singleFieldColumn;
    private Column nullableColumn;
    private Column hasRelationshipColumn;
    private Column multiFieldColumn;

    @BeforeEach
    void setup() {
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
    void isMultiFieldTest() {
        assertTrue(multiFieldColumn.isMultiField());
        assertFalse(singleFieldColumn.isMultiField());
        assertFalse(nullableColumn.isMultiField());
        assertFalse(hasRelationshipColumn.isMultiField());
    }

    @Test
    void isNullableTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getField() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.isNullable());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void isNullableTest() {
        assertTrue(nullableColumn.isNullable());
        assertFalse(singleFieldColumn.isNullable());
        assertFalse(hasRelationshipColumn.isNullable());
    }

    @Test
    void hasRelationshipTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getField() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.hasRelationship());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void hasRelationshipTest() {
        assertTrue(hasRelationshipColumn.hasRelationship());
        assertTrue(nullableColumn.hasRelationship());
        assertFalse(singleFieldColumn.hasRelationship());
    }

    @Test
    void getFieldTest() {
        assertEquals("field", singleFieldColumn.getField());
        assertEquals("entity.field", nullableColumn.getField());
        assertEquals("entity.field", hasRelationshipColumn.getField());
        assertEquals("firstField secondField", multiFieldColumn.getField());
    }

    @Test
    void getFieldListTest() {
        assertIterableEquals(List.of("firstField", "secondField"), multiFieldColumn.getFieldList());
    }

    @Test
    void getEntityFieldTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getField() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.getEntityField());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getEntityFieldTestNoRelationshipInvalidCall() {
        String message = "Column '" + singleFieldColumn.getField() + "' has no relationship.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getEntityField());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getEntityFieldTest() {
        assertEquals("entity", hasRelationshipColumn.getEntityField());
        assertEquals("entity", nullableColumn.getEntityField());
    }

    @Test
    void getSubColumnListTestNotMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getField() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getSubColumnList());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getSubColumnListTest() {
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
    void getLeftJoinClauseTestMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getField() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getSubColumnList());
        assertEquals(exception.getMessage(), message);
    }
}
