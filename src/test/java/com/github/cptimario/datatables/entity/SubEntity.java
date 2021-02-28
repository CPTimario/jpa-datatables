package com.github.cptimario.datatables.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class SubEntity {
    @Id
    Integer id;

    String firstData;

    String secondData;

    Date date;
}
