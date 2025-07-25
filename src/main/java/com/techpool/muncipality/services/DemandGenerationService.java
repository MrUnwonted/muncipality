package com.techpool.muncipality.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techpool.muncipality.entity.BuildingMaster;
import com.techpool.muncipality.entity.DemandRegister;
import com.techpool.muncipality.repository.BuildingMasterRepository;
import com.techpool.muncipality.repository.DemandRegisterRepository;

@Service
public class DemandGenerationService {

    @Autowired
    private BuildingMasterRepository buildingMasterRepository;

    @Autowired
    private DemandRegisterRepository demandRegisterRepository;

    private final Random random = new Random();

    public void generateInitialDemandData() {
        try {
            // Get all buildings
            List<BuildingMaster> allBuildings = buildingMasterRepository.findAll();
            if (allBuildings.isEmpty()) {
                throw new RuntimeException("Building Master table is empty");
            }

            // Rest of your existing code remains the same...
            int totalBuildings = allBuildings.size();
            int buildingsToProcess = (int) Math.round(totalBuildings * 0.5);

            List<BuildingMaster> selectedBuildings = allBuildings.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                Collections.shuffle(list);
                                return list.stream();
                            }))
                    .limit(buildingsToProcess)
                    .collect(Collectors.toList());

            List<DemandRegister> demandRegisters = selectedBuildings.stream()
                    .map(this::createDemandFor2016)
                    .collect(Collectors.toList());

            demandRegisterRepository.saveAll(demandRegisters);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate demand data: " + e.getMessage(), e);
        }
    }

    private DemandRegister createDemandFor2016(BuildingMaster building) {
        DemandRegister demand = new DemandRegister();

        // Set Building ID (Using the Id from the buidling master, not the buidling
        // String)
        demand.setFk_building_id(building.getId());

        // Set year to 201t6
        demand.setInt_year_id(2016);

        // Set period (assuming 1 is the main period, could be adjusted)
        demand.setInt_period_id(1);

        // Set Tax Rate
        BigDecimal taxRate = new BigDecimal(building.getTaxRate());
        demand.setNum_tax_rate(taxRate);

        // Set LC (Library Cess) as 10% of tax rate
        BigDecimal lc = taxRate.multiply(new BigDecimal("0.10"));
        demand.setNum_lc(lc);

        // Randomly decide if this is an arrear (10% chance)
        demand.setTny_arrear_flag(random.nextDouble() < 0.1);

        // For records with receipt (70% chance of having a receipt)
        if (random.nextDouble() < 0.7) {
            // Generate random date between March 2015 and March 2016
            LocalDate receiptDate = generateRandomDate(
                    LocalDate.of(2015, Month.MARCH, 1),
                    LocalDate.of(2016, Month.MARCH, 31));
            demand.setDt_receipt(receiptDate);

            // Generate a random receipt ID between 1000 and 9999
            demand.setInt_receipt_id(1000 + random.nextInt(9000));
        } else {
            demand.setDt_receipt(null);
            demand.setInt_receipt_id(null);
        }

        return demand;
    }

    private LocalDate generateRandomDate(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();
        long randomDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomDay);
    }

}
