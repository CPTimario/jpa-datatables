package io.github.cptimario.datatables.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColumnTest {
    private Column singleFieldColumn;
    private Column hasRelationshipColumn;
    private Column multiFieldColumn;

    @BeforeEach
    void setup() {
        singleFieldColumn = new Column("field");
        hasRelationshipColumn = new Column("entity.field");
        multiFieldColumn = new Column("firstField + secondField");
    }

    @Test
    void isMultiFieldTest() {
        assertTrue(multiFieldColumn.isMultiField());
        assertFalse(singleFieldColumn.isMultiField());
        assertFalse(hasRelationshipColumn.isMultiField());
    }

    @Test
    void hasRelationshipTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getData() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.hasRelationship());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void hasRelationshipTest() {
        assertTrue(hasRelationshipColumn.hasRelationship());
        assertFalse(singleFieldColumn.hasRelationship());
    }

    @Test
    void getFieldListTest() {
        assertIterableEquals(List.of("firstField", "secondField"), multiFieldColumn.getFieldList());
    }

    @Test
    void getEntityFieldTestMultiFieldInvalidCall() {
        String message = "Column '" + multiFieldColumn.getData() + "' is a multi-field column.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> multiFieldColumn.getBaseField());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getEntityFieldTestNoRelationshipInvalidCall() {
        String message = "Column '" + singleFieldColumn.getData() + "' has no relationship.";
        Throwable exception = assertThrows(IllegalCallerException.class, () -> singleFieldColumn.getBaseField());
        assertEquals(exception.getMessage(), message);
    }

    @Test
    void getEntityFieldTest() {
        assertEquals("entity", hasRelationshipColumn.getBaseField());
    }

    @Test
    void getSubColumnListTestNotMultiFieldInvalidCall() {
        assertNull(singleFieldColumn.getSubColumnList());
    }

    @Test
    void getSubColumnListTest() {
        Column firstSubColumn = new Column("firstField");
        Column secondSubColumn = new Column("secondField");
        assertIterableEquals(List.of(firstSubColumn, secondSubColumn), multiFieldColumn.getSubColumnList());
    }

    @Test
    void getFieldDelimiterTest() {
        assertEquals("", singleFieldColumn.getFieldDelimiter());
        assertEquals(" + ", multiFieldColumn.getFieldDelimiter());
    }
}
