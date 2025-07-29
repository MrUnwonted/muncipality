package com.techpool.muncipality.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class BuildingMaster {
    @Id
    @Column(length = 17, unique = true, nullable = false)
    private String buildingId;

    private String doorNumber;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    private String taxRate;

    private String squareFeet;

    private boolean isCommercial;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemandRegister> demands = new ArrayList<>();

}
