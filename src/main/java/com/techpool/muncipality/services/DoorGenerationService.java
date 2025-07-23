package com.techpool.muncipality.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techpool.muncipality.entity.Door;
import com.techpool.muncipality.entity.Ward;
import com.techpool.muncipality.entity.Zone;
import com.techpool.muncipality.repository.DoorRepository;
import com.techpool.muncipality.repository.WardRepository;
import com.techpool.muncipality.repository.ZoneRepository;

@Service
public class DoorGenerationService {
    @Autowired
    ZoneRepository zoneRepo;
    @Autowired
    WardRepository wardRepo;
    @Autowired
    DoorRepository doorRepo;

    private final String[] suffixes = { "", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "O" };
    private final int maxBase = 999; // Sequential bases: 1-999. Adjust if you want longer/shorter numbering.

    public List<Map<String, Object>> generate(int zonesCount, int wardsPerZone, int totalLimit) {
        List<Zone> zones = new ArrayList<>();
        List<Ward> wards = new ArrayList<>();
        List<Door> allDoors = new ArrayList<>();
        int totalWards = zonesCount * wardsPerZone;
        int doorsPerWardBase = totalLimit / totalWards;
        int remainder = totalLimit % totalWards;

        // 1. Create zones and wards
        for (int z = 1; z <= zonesCount; z++) {
            Zone zone = new Zone();
            zone.setMunicipalityName("Kollam Municipality " + z);
            zone.setDescription("Zone " + z);
            zone.setDistrict("Kollam");
            zone = zoneRepo.save(zone);
            zones.add(zone);
            for (int w = 1; w <= wardsPerZone; w++) {
                Ward ward = new Ward();
                ward.setZone(zone);
                ward.setDescription("Ward " + w);
                ward = wardRepo.save(ward);
                wards.add(ward);
            }
        }

        // 2. For each ward, assign doors sequentially with suffix as needed
        int wardIndex = 0;
        for (Ward ward : wards) {
            int doorsForThisWard = doorsPerWardBase;
            if (wardIndex < remainder)
                doorsForThisWard++; // balance the remainder
            wardIndex++;

            int base = 1;
            int suffixIndex = 0;

            for (int i = 0; i < doorsForThisWard; i++) {
                if (base > maxBase) {
                    base = 1;
                    suffixIndex++;
                    if (suffixIndex >= suffixes.length) {
                        throw new IllegalStateException(
                                "Not enough suffixes for required door numbers in ward " + ward.getId());
                    }
                }
                String doorNum = base + suffixes[suffixIndex];
                base++;

                Door door = new Door();
                door.setDoorNumber(doorNum);
                door.setWard(ward);
                door.setZone(ward.getZone());
                allDoors.add(door);
            }
        }
        doorRepo.saveAll(allDoors);

        // 3. Prepare result
        return allDoors.stream().map(door -> {
            Map<String, Object> map = new HashMap<>();
            map.put("zoneNumber", door.getZone().getId());
            map.put("wardNumber", door.getWard().getId());
            map.put("doorNumber", door.getDoorNumber());
            map.put("municipalityName", door.getZone().getMunicipalityName());
            return map;
        }).toList();
    }
}
