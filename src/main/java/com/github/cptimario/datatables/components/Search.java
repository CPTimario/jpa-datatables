package com.github.cptimario.datatables.components;

import java.util.Objects;

public class Search {
    private String value;
    private boolean regex;

    public Search() {
        this("", false);
    }

    public Search(String value) {
        this(value, false);
    }

    public Search(String value, boolean isRegex) {
        this.value = value;
        this.regex = isRegex;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Search)) return false;
        Search search = (Search) o;
        return regex == search.regex && Objects.equals(value, search.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, regex);
    }
}
