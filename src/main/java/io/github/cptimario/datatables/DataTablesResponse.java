package io.github.cptimario.datatables;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/**
 * DataTablesResponse class contains the datatables response to be sent back to the client side
 *
 * @param <E> the entity class type
 * @author Christopher Timario
 * @version v1.0.0
 */
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
