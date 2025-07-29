package com.techpool.muncipality.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class DemandRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long int_demand_id;

    // @Column(precision = 18, scale = 0)
    // private Long fk_building_id;

    private int int_year_id;

    private int int_period_id;

    private BigDecimal num_tax_rate;

    private BigDecimal num_lc;

    private boolean tny_arrear_flag;

    private Integer int_receipt_id;

    private LocalDate dt_receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", referencedColumnName = "buildingId", nullable = false)
    private BuildingMaster building;
}
