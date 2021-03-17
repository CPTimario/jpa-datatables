package com.github.cptimario.datatables.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class ChildEntity {
    @Id
    private Integer id;

    private String firstData;

    private String secondData;

    private Date date;
}
