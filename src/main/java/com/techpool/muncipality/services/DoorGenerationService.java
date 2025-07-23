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
import com.techpool.muncipality.entity.Door;
import com.techpool.muncipality.entity.Ward;
import com.techpool.muncipality.repository.DoorRepository;
import com.techpool.muncipality.repository.WardRepository;

@Service
public class DoorGenerationService {
    @Autowired
    WardRepository wardRepo;
    @Autowired
    DoorRepository doorRepo;

    private final String[] suffixes = { "", "A", "B", "C", "D", "E", "F" }; // can be extended
    private final int maxBase = 999; // 1 to 999: common max for municipalities

    public List<Map<String, Object>> generate(int totalLimit) {
        System.out.println("Starting.........");
        List<Ward> wards = wardRepo.findAll();
        List<Door> allDoors = new ArrayList<>();
        int totalWards = wards.size();
        int doorsPerWardBase = totalLimit / totalWards;
        int remainder = totalLimit % totalWards;
        int wardIndex = 0;
        Random rand = new Random();

        for (Ward ward : wards) {
            int doorsForThisWard = doorsPerWardBase;
            if (wardIndex < remainder)
                doorsForThisWard++;
            wardIndex++;

            // -- Core logic for proper municipal door numbers:
            // Per ward, sequential unique numbers with progressive suffixes only if needed
            Map<Integer, Integer> baseUsage = new HashMap<>();
            int nextNum = 1;
            Set<String> used = new HashSet<>();
            int doorsCreated = 0;
            while (doorsCreated < doorsForThisWard) {
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
                    Door door = new Door();
                    door.setDoorNumber(doorNum);
                    door.setWard(ward);
                    door.setZone(ward.getZone());
                    allDoors.add(door);
                    doorsCreated++;
                }
            }
        }
        doorRepo.saveAll(allDoors);
        System.out.println("Created");

        // API/display output
        return allDoors.stream().map(door -> {
            Map<String, Object> map = new HashMap<>();
            map.put("zoneId", door.getZone().getId());
            map.put("wardId", door.getWard().getId());
            map.put("wardName", door.getWard().getName());
            map.put("zoneName", door.getZone().getName());
            map.put("doorNumber", door.getDoorNumber());
            return map;
        }).toList();
    }
}
