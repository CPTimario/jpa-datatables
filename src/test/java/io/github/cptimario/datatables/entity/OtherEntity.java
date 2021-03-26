package io.github.cptimario.datatables.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
public class OtherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstData;

    private String secondData;

    private LocalDate date;

    @ManyToOne(cascade = CascadeType.ALL)
    private ChildEntity childEntity;
}
