package com.github.cptimario.datatables.entity;

import javax.persistence.*;

@Entity
public class ParentEntity {
    @Id
    private Integer id;

    private String data;

    @ManyToOne
    private ChildEntity firstChildEntity;

    @ManyToOne
    private ChildEntity secondChildEntity;

    @OneToOne
    private ChildEntity thirdChildEntity;
}
