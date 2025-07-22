package com.techpool.muncipality.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String[] SUFFIXES = { "", "A", "B", "C" };
    private final Random random = new Random();

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
            // For each ward in this zone
            for (int w = 1; w <= wardsPerZone; w++) {
                Ward ward = new Ward();
                ward.setZone(zone);
                ward.setDescription("Ward " + w);
                ward = wardRepo.save(ward);
                wards.add(ward);
            }
        }

        // 2. For each ward, assign doors
        int wardIndex = 0;
        for (Ward ward : wards) {
            int doorsForThisWard = doorsPerWardBase;
            // Distribute the remainder randomly or sequentially
            if (wardIndex < remainder)
                doorsForThisWard++;
            // Optional: Add a tiny random fluctuation (e.g. ±2)
            // doorsForThisWard += random.nextInt(-2, 3);

            Set<String> generated = new HashSet<>();
            for (int i = 0; i < doorsForThisWard; i++) {
                String doorNum;
                do {
                    int base = random.nextInt(100, 9999);
                    String suffix = SUFFIXES[random.nextInt(SUFFIXES.length)];
                    doorNum = base + suffix;
                } while (!generated.add(doorNum));

                Door door = new Door();
                door.setDoorNumber(doorNum);
                door.setWard(ward);
                door.setZone(ward.getZone());
                allDoors.add(door);
            }
            wardIndex++;
        }
        doorRepo.saveAll(allDoors);

        return allDoors.stream().map(door -> {
            Map<String, Object> map = new HashMap<>();
            map.put("zoneNumber", door.getZone().getId());
            map.put("wardNumber", door.getWard().getId());
            map.put("doorNumber", door.getDoorNumber());
            map.put("municipalityName", door.getZone().getMunicipalityName());
            return map;
        }).collect(Collectors.toList());

    }
}
