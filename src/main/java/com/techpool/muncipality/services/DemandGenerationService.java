package com.techpool.muncipality.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
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
    private static final double PAYING_PERCENTAGE = 0.7;
    // private static final int START_YEAR = 2016;
    // private static final int END_YEAR = 2025;

    public void generateMultiYearDemandData(int startYear, int endYear) {
        try {
            List<BuildingMaster> allBuildings = buildingMasterRepository.findAll();
            if (allBuildings.isEmpty()) {
                throw new RuntimeException("Building Master table is empty");
            }

            // Generate initial data for startYear (50% of buildings)
            generateInitialDemandData(startYear);

            // Generate subsequent years
            for (int year = startYear + 1; year <= endYear; year++) {
                generateYearlyDemandData(year);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate demand data: " + e.getMessage(), e);
        }
    }

    private void generateInitialDemandData(int year) {
        List<BuildingMaster> allBuildings = buildingMasterRepository.findAll();
        int buildingsToProcess = (int) Math.round(allBuildings.size() * 0.5);

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
                .map(building -> createDemandForYear(building, year))
                .collect(Collectors.toList());

        demandRegisterRepository.saveAll(demandRegisters);
    }

    private void generateYearlyDemandData(int year) {
        // Get all buildings that paid in previous year
        List<DemandRegister> previousYearDemands = demandRegisterRepository.findByYear(year - 1);

        // Calculate 70% of previous year's paying buildings
        int payingBuildingsCount = (int) Math.ceil(previousYearDemands.size() * PAYING_PERCENTAGE);

        // Shuffle and select the required number of buildings
        Collections.shuffle(previousYearDemands);
        List<DemandRegister> selectedDemands = previousYearDemands.stream()
                .limit(payingBuildingsCount)
                .collect(Collectors.toList());

        // Create new demands for current year based on previous year's data
        List<DemandRegister> newDemands = selectedDemands.stream()
                .map(previousDemand -> createNextYearDemand(previousDemand, year))
                .collect(Collectors.toList());

        demandRegisterRepository.saveAll(newDemands);
    }

    // private DemandRegister createDemandForYear(BuildingMaster building) {
    // return createDemandForYear(building, START_YEAR);
    // }

    private DemandRegister createDemandForYear(BuildingMaster building, int year) {
        // Add validation
        Objects.requireNonNull(building.getBuildingId(), "Building must be persisted first");
        DemandRegister demand = new DemandRegister();
        demand.setBuilding(building);
        demand.setInt_year_id(year);
        demand.setInt_period_id(1);

        BigDecimal taxRate = new BigDecimal(building.getTaxRate());
        demand.setNum_tax_rate(taxRate);

        BigDecimal lc = taxRate.multiply(new BigDecimal("0.10"));
        demand.setNum_lc(lc);

        demand.setTny_arrear_flag(random.nextDouble() < 0.1);

        if (random.nextDouble() < 0.7) {
            LocalDate receiptDate = generateRandomDate(
                    LocalDate.of(year - 1, Month.MARCH, 1),
                    LocalDate.of(year, Month.MARCH, 31));
            demand.setDt_receipt(receiptDate);
            demand.setInt_receipt_id(1000 + random.nextInt(9000));
        } else {
            demand.setDt_receipt(null);
            demand.setInt_receipt_id(null);
        }

        return demand;
    }

    private DemandRegister createNextYearDemand(DemandRegister previousDemand, int newYear) {
        DemandRegister newDemand = new DemandRegister();

        // Copy basic information
        newDemand.setBuilding(previousDemand.getBuilding());
        newDemand.setInt_year_id(newYear);
        newDemand.setInt_period_id(previousDemand.getInt_period_id());

        // Apply inflation to tax rate (5% per year)
        BigDecimal inflationFactor = new BigDecimal("1.05");
        newDemand.setNum_tax_rate(previousDemand.getNum_tax_rate().multiply(inflationFactor));
        newDemand.setNum_lc(newDemand.getNum_tax_rate().multiply(new BigDecimal("0.10")));

        // Arrear logic - higher chance if previous was unpaid
        boolean previousUnpaid = !isPaid(previousDemand);
        newDemand.setTny_arrear_flag(previousUnpaid ? random.nextDouble() < 0.8 : // 80% chance if unpaid
                random.nextDouble() < 0.1); // 10% chance if paid

        // Payment generation (70% chance)
        if (random.nextDouble() < 0.7) {
            LocalDate receiptDate = generateRandomDate(
                    LocalDate.of(newYear - 1, Month.MARCH, 1),
                    LocalDate.of(newYear, Month.MARCH, 31));
            newDemand.setDt_receipt(receiptDate);
            newDemand.setInt_receipt_id(1000 + random.nextInt(9000));
        } else {
            newDemand.setDt_receipt(null);
            newDemand.setInt_receipt_id(null);
        }

        return newDemand;
    }

    private boolean isPaid(DemandRegister demand) {
        return demand.getDt_receipt() != null && demand.getInt_receipt_id() != null;
    }

    private LocalDate generateRandomDate(LocalDate startInclusive, LocalDate endExclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endExclusive.toEpochDay();
        long randomDay = startEpochDay + random.nextInt((int) (endEpochDay - startEpochDay));
        return LocalDate.ofEpochDay(randomDay);
    }
}