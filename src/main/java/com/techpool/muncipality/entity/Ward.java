package com.techpool.muncipality.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="zone_id")
    private Zone zone;

    private String description;

}
