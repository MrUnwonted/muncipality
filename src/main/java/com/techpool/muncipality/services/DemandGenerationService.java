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

            // Select buildings once for the entire period (consistent selection)
            List<BuildingMaster> selectedBuildings = selectBuildings(allBuildings);
            // Generate complete timeline for each selected building
            for (BuildingMaster building : selectedBuildings) {
                generateBuildingDemandTimeline(building, startYear, endYear);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate demand data: " + e.getMessage(), e);
        }
    }

    private List<BuildingMaster> selectBuildings(List<BuildingMaster> allBuildings) {
        // Select ~50% of buildings (adjust percentage as needed)
        Collections.shuffle(allBuildings);
        return allBuildings.stream()
                .limit((long) (allBuildings.size() * 0.5))
                .collect(Collectors.toList());
    }

    private void generateBuildingDemandTimeline(BuildingMaster building, int startYear, int endYear) {
        List<DemandRegister> buildingDemands = new ArrayList<>();

        // First year gets initial values
        DemandRegister previousDemand = createInitialDemand(building, startYear);
        buildingDemands.add(previousDemand);

        // Subsequent years build on previous
        for (int year = startYear + 1; year <= endYear; year++) {
            DemandRegister currentDemand = createNextYearDemand(previousDemand, year);
            buildingDemands.add(currentDemand);
            previousDemand = currentDemand;
        }

        demandRegisterRepository.saveAll(buildingDemands);
    }

    private DemandRegister createInitialDemand(BuildingMaster building, int year) {
        DemandRegister demand = new DemandRegister();
        demand.setBuilding(building);
        demand.setInt_year_id(year);
        demand.setInt_period_id(1); // Assuming period 1 is standard

        // Set tax values
        BigDecimal taxRate = new BigDecimal(building.getTaxRate());
        demand.setNum_tax_rate(taxRate);
        demand.setNum_lc(taxRate.multiply(new BigDecimal("0.10")));

        // Payment details (always generated for this approach)
        demand.setDt_receipt(generatePaymentDate(year));
        demand.setInt_receipt_id(1000 + random.nextInt(9000));

        // First year never has arrears
        demand.setTny_arrear_flag(false);

        return demand;
    }

    private DemandRegister createNextYearDemand(DemandRegister previousDemand, int newYear) {
        DemandRegister newDemand = new DemandRegister();

        // Copy basic info
        newDemand.setBuilding(previousDemand.getBuilding());
        newDemand.setInt_year_id(newYear);
        newDemand.setInt_period_id(previousDemand.getInt_period_id());

        // Apply inflation
        BigDecimal inflationFactor = new BigDecimal("1.05");
        newDemand.setNum_tax_rate(previousDemand.getNum_tax_rate().multiply(inflationFactor));
        newDemand.setNum_lc(newDemand.getNum_tax_rate().multiply(new BigDecimal("0.10")));

        // Payment details (always generated)
        newDemand.setDt_receipt(generatePaymentDate(newYear));
        newDemand.setInt_receipt_id(1000 + random.nextInt(9000));

        // Arrears only if previous payment was late (optional logic)
        LocalDate prevPayment = previousDemand.getDt_receipt();
        boolean prevWasLate = prevPayment != null &&
                prevPayment.getMonthValue() > 3; // Paid after March
        newDemand.setTny_arrear_flag(prevWasLate && random.nextDouble() < 0.3);

        return newDemand;
    }

    private LocalDate generatePaymentDate(int year) {
        // Generate date between April 1 (current year) and March 31 (next year)
        // This ensures payment date matches the financial year
        int startMonth = random.nextBoolean() ? 4 : 1; // 50% chance current/next year
        if (startMonth == 4) {
            // Current year payment (April-Dec)
            int month = 4 + random.nextInt(9); // April-December
            int day = 1 + random.nextInt(Month.of(month).length(false) - 1);
            return LocalDate.of(year, month, day);
        } else {
            // Next year payment (Jan-Mar)
            int month = 1 + random.nextInt(3); // January-March
            int day = 1 + random.nextInt(Month.of(month).length(false) - 1);
            return LocalDate.of(year + 1, month, day);
        }
    }
    // private DemandRegister createDemandForYear(BuildingMaster building) {
    // return createDemandForYear(building, START_YEAR);
    // }

}