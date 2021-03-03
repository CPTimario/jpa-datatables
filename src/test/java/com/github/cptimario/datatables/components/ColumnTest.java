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
    void isNullableTest() {
        assertTrue(nullableColumn.isNullable());
        assertFalse(singleFieldColumn.isNullable());
        assertFalse(hasRelationshipColumn.isNullable());
    }

    @Test
    void hasRelationship() {
        assertTrue(hasRelationshipColumn.hasRelationship());
        assertTrue(nullableColumn.hasRelationship());
        assertFalse(singleFieldColumn.hasRelationship());
    }

    @Test
    void getFullFieldNameTest() {
        assertEquals("field", singleFieldColumn.getFullFieldName());
        assertEquals("entity.field", nullableColumn.getFullFieldName());
        assertEquals("entity.field", hasRelationshipColumn.getFullFieldName());
        assertEquals("firstField secondField", multiFieldColumn.getFullFieldName());
    }

    @Test
    void getFullFieldNameListTestNotMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getFullFieldNameList());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getFullFieldNameListTest() {
        assertIterableEquals(List.of("firstField", "secondField"), multiFieldColumn.getFullFieldNameList());
    }

    @Test
    void getRelationshipFieldNameTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getFullFieldName() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.getRelationshipFieldName());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getRelationshipFieldNameTestNoRelationshipInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' has no relationship.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getRelationshipFieldName());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getRelationshipFieldNameTest() {
        assertEquals("entity", hasRelationshipColumn.getRelationshipFieldName());
    }

    @Test
    void getSubColumnListTestNotMultiFieldInvalidCall() {
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' is not a multi-field column.";
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
        String message = "Column '" + singleFieldColumn.getFullFieldName() + "' is not a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getSubColumnList());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getLeftJoinClauseTest() {
        String joinClause = " Left Join left." + hasRelationshipColumn.getRelationshipFieldName() + " right";
        assertEquals(joinClause, hasRelationshipColumn.getLeftJoinClause("left", "right"));
    }
}
