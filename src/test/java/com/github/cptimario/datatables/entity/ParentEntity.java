package com.github.cptimario.datatables.entity;

import javax.persistence.*;

@Entity
public class ParentEntity {
    @Id
    Integer id;

    String data;

    @ManyToOne
    ChildEntity firstChildEntity;

    @ManyToOne
    ChildEntity secondChildEntity;
}
