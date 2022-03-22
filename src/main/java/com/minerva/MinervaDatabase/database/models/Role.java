package com.minerva.MinervaDatabase.database.models;

import lombok.Data;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ERole name;
}
