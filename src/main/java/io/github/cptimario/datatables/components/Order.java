package io.github.cptimario.datatables.components;

import lombok.Data;

@Data
public class Order {
    private int column;
    private String dir;

    public Order() {
        this(0, "asc");
    }

    public Order(int columnIndex, String direction) {
        setColumn(columnIndex);
        setDir(direction);
    }

    public void setDir(String dir) {
        if (!"asc".equalsIgnoreCase(dir) && !"desc".equalsIgnoreCase(dir)) {
            throw new IllegalArgumentException("'" + dir + "' not a valid direction.");
        }
        this.dir = dir;
    }
}
