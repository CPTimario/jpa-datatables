package io.github.cptimario.datatables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class DataTablesResponse<E> {
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List<?> data;
    private String error;

    @JsonIgnore
    private List<E> resultList;
}
