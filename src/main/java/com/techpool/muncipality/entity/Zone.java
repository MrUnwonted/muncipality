package com.techpool.muncipality.entity;

import jakarta.persistence.*;
import lombok.Data;
 
@Entity
@Data
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    // Optionally: extra details (use if your DB has these columns)
    // private String description;
    // private String district;
}

