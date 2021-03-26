package com.github.cptimario.datatables.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
public class ParentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String data;

    private LocalDate date;

    @ManyToOne(cascade = CascadeType.ALL)
    private ChildEntity childEntity;

    @OneToOne(cascade = CascadeType.ALL)
    private OtherEntity otherEntity;
}
