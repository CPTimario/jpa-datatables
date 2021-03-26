package com.github.cptimario.datatables.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    private Order order;

    @Test
    void setDirTest() {
        order = new Order();
        order.setDir("asc");
        assertTrue("asc".equalsIgnoreCase(order.getDir()));
        order.setDir("ASC");
        assertTrue("asc".equalsIgnoreCase(order.getDir()));
        order.setDir("desc");
        assertTrue("desc".equalsIgnoreCase(order.getDir()));
        order.setDir("DESC");
        assertTrue("desc".equalsIgnoreCase(order.getDir()));
    }

    @Test
    void setDirTestInvalidInput() {
        order = new Order();
        String message = "'invalid' not a valid direction.";
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> order.setDir("invalid"));
        assertEquals(message, exception.getMessage());
    }
}
