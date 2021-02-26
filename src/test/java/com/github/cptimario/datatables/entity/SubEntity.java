package com.github.cptimario.datatables.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SubEntity {
    @Id
    Integer id;

    String firstData;

    String secondData;
}
