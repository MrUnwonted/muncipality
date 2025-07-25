package com.techpool.muncipality.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.techpool.muncipality.entity.BuildingMaster;
import com.techpool.muncipality.entity.Ward;
import com.techpool.muncipality.repository.BuildingMasterRepository;
import com.techpool.muncipality.repository.WardRepository;

@Service
public class DoorGenerationService {
    @Autowired
    WardRepository wardRepo;
    @Autowired
    BuildingMasterRepository buildingRepo;

    private final String[] suffixes = { "", "A", "B", "C", "D", "E", "F" }; // can be extended
    // private final int maxBase = 999; // 1 to 999: common max for municipalities
    // Define square feet ranges and corresponding tax rates (half-year rates)
    private static final int[][] SQUARE_FEET_RANGES_AND_TAX = {
            { 800, 1000, 100 },
            { 1001, 1500, 150 },
            { 1501, 2000, 200 },
            { 2001, 2500, 250 },
            { 2501, 3000, 300 },
            { 3001, 4000, 350 }
    };
    private static final int MIN_SQ_FEET = 800;
    private static final int MAX_SQ_FEET = 4000;
    private static final double COMMERCIAL_PROBABILITY = 0.2; // 30% chance of being commercial

    public List<Map<String, Object>> generate(int totalLimit) {
        System.out.println("Starting.........");
        List<Ward> wards = wardRepo.findAll();
        List<BuildingMaster> allDoors = new ArrayList<>();
        int totalWards = wards.size();
        int doorsPerWardBase = totalLimit / totalWards;
        int remainder = totalLimit % totalWards;
        int wardIndex = 0;
        Random rand = new Random();

        long globalSeq = 1; // Seq for building id, unique across all doors

        for (Ward ward : wards) {
            int doorsForThisWard = doorsPerWardBase;
            if (wardIndex < remainder)
                doorsForThisWard++;
            wardIndex++;

            // -- Core logic for proper municipal door numbers:
            // Per ward, sequential unique numbers with progressive suffixes only if needed
            Map<Integer, Integer> baseUsage = new HashMap<>();
            Set<String> used = new HashSet<>();
            int nextNum = 1;
            int doorsCreated = 0;
            while (doorsCreated < doorsForThisWard) {
                // --- Door Number Logic (realistic, with rare suffixes) ---
                String doorNum;

                // For realism: 90-95% just get the next sequential number
                if (rand.nextDouble() > 0.07 || nextNum == 1) {
                    doorNum = Integer.toString(nextNum);
                    nextNum++;
                } else {
                    // Suffix case: pick a prior number and assign next unused suffix to it
                    int baseForSuffix = rand.nextInt(nextNum - 1) + 1; // choose from already used base numbers
                    int suffixIdx = 1;
                    while (used.contains(baseForSuffix + suffixes[suffixIdx]) && suffixIdx < suffixes.length - 1) {
                        suffixIdx++;
                    }
                    doorNum = baseForSuffix + suffixes[suffixIdx];
                    // if all suffixes used, fallback to next unique number
                    if (used.contains(doorNum)) {
                        doorNum = Integer.toString(nextNum);
                        nextNum++;
                    }
                }
                if (used.add(doorNum)) {
                    // --- Building Id Logic ---
                    // zoneCode: must be present in your zone table/entity
                    String zoneCode = String.format("%04d", ward.getZone().getId()); // 4-Digit
                    String wardCode = String.format("%04d", ward.getId()); // 4-Digit
                    int year = 1950 + rand.nextInt(2011 - 1950); // [1950, 2010]
                    String yearCode = String.format("%02d", year % 100);
                    String uniqueSeq = String.format("%07d", globalSeq++);
                    String buildingId = zoneCode + wardCode + yearCode + uniqueSeq; // 17 digit

                    int squareFeet = MIN_SQ_FEET + rand.nextInt(MAX_SQ_FEET - MIN_SQ_FEET + 1);
                    int taxRate = calculateTaxRate(squareFeet, true);
                    boolean isCommercial = rand.nextDouble() < COMMERCIAL_PROBABILITY;

                    // Apply commercial multiplier if needed (commercial properties pay 1.5x tax)
                    // if (isCommercial) {
                    // taxRate = (int) Math.round(taxRate * 1.5);
                    // }

                    BuildingMaster door = new BuildingMaster();
                    door.setDoorNumber(doorNum);
                    door.setWard(ward);
                    door.setZone(ward.getZone());
                    door.setBuildingId(buildingId);
                    door.setSquareFeet(String.valueOf(squareFeet));
                    door.setTaxRate(String.valueOf(taxRate));
                    door.setCommercial(isCommercial);
                    allDoors.add(door);
                    doorsCreated++;
                }
            }
        }
        buildingRepo.saveAll(allDoors);
        System.out.println("Created");

        // API/display output
        return allDoors.stream().map(door -> {
            Map<String, Object> map = new HashMap<>();
            map.put("zoneId", door.getZone().getId());
            map.put("wardId", door.getWard().getId());
            map.put("wardName", door.getWard().getName());
            map.put("zoneName", door.getZone().getName());
            map.put("doorNumber", door.getDoorNumber());
            map.put("buildingId", door.getBuildingId());
            map.put("squareFeet", door.getSquareFeet());
            map.put("taxRate", door.getTaxRate());
            map.put("isCommercial", door.isCommercial());
            return map;
        }).toList();
    }

    private int calculateTaxRate(int squareFeet, boolean isHalfYear) {
        for (int[] rangeAndTax : SQUARE_FEET_RANGES_AND_TAX) {
            if (squareFeet >= rangeAndTax[0] && squareFeet <= rangeAndTax[1]) {
                return isHalfYear ? rangeAndTax[2] : rangeAndTax[2] * 2;
            }
        }
        // Fallback
        return isHalfYear ? SQUARE_FEET_RANGES_AND_TAX[SQUARE_FEET_RANGES_AND_TAX.length - 1][2]
                : SQUARE_FEET_RANGES_AND_TAX[SQUARE_FEET_RANGES_AND_TAX.length - 1][2] * 2;
    }
}
