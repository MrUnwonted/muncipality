package com.techpool.muncipality.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BuildingMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String doorNumber;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(length = 17, unique = true, nullable = false)
    private String buildingId;

    private String taxRate;

    private String squareFeet;

    private boolean isCommercial;

}
