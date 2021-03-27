package io.github.cptimario.datatables.components;

import lombok.Data;

/**
 * Search class handles the storage and manipulation of the datatables search parameters received from the client side
 *
 * @author Christopher Timario
 * @version v1.0.0
 */
@Data
public class Search {
    private String value;
    private boolean regex;

    /**
     * Creates an instance of datatables search parameter with the specified search text value
     *
     * @param value the value of the search text parameter
     */
    public Search(String value) {
        this(value, false);
    }

    /**
     * Creates an instance of datatables search parameter with the specified search text value and the flag for searching using regex
     *
     * @param value   the value of the search text parameter
     * @param isRegex the flag for using regex in the search process
     */
    public Search(String value, boolean isRegex) {
        setValue(value);
        setRegex(isRegex);
    }
}
