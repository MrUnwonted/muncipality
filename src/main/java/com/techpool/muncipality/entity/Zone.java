package com.techpool.muncipality.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String municipalityName;
    private String description;
    private String district;

}
