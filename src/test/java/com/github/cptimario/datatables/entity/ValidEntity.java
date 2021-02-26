package com.github.cptimario.datatables.entity;

import javax.persistence.*;

@Entity
public class ValidEntity {
    @Id
    Integer id;

    String data;

    @ManyToOne
    SubEntity firstSubEntity;

    @ManyToOne
    SubEntity secondSubEntity;
}
