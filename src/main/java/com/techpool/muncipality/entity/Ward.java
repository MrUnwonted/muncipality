package com.techpool.muncipality.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @ManyToOne
    @JoinColumn(name="zone_id")
    private Zone zone;

    // Optionally: extra details (use if present)
    // private String description;
}

