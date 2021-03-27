package io.github.cptimario.datatables.components;

import lombok.Data;

/**
 * Order class handles the storage and manipulation of the datatables order parameters received from the client side
 *
 * @author Christopher Timario
 * @version v1.0.0
 */
@Data
public class Order {
    private int column;
    private String dir;

    /**
     * Creates an instance of datatables order parameter with the default values
     */
    public Order() {
        this(0, "asc");
    }

    /**
     * Creates an instance of datatables order parameter with the specified column index and order direction
     *
     * @param columnIndex the column index
     * @param direction   the order direction
     */
    public Order(int columnIndex, String direction) {
        setColumn(columnIndex);
        setDir(direction);
    }

    /**
     * Sets the order direction
     *
     * @param dir the order direction
     * @throws IllegalArgumentException if the specified direction is invalid
     */
    public void setDir(String dir) {
        if (!"asc".equalsIgnoreCase(dir) && !"desc".equalsIgnoreCase(dir)) {
            throw new IllegalArgumentException("'" + dir + "' not a valid direction.");
        }
        this.dir = dir;
    }
}
