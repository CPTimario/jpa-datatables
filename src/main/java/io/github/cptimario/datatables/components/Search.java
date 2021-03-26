package io.github.cptimario.datatables.components;

import lombok.Data;

@Data
public class Search {
    private String value;
    private boolean regex;

    public Search(String value) {
        this(value, false);
    }

    public Search(String value, boolean isRegex) {
        setValue(value);
        setRegex(isRegex);
    }
}
