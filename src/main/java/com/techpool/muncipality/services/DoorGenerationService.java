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

    private final String[] suffixes = { "", "A", "B", "C", "D", "E",  };
    private final int maxBase = 999; // or adjust as needed

    public List<Map<String, Object>> generate(int totalLimit) {

        List<Ward> wards = wardRepo.findAll();
        List<Door> allDoors = new ArrayList<>();
        int totalWards = wards.size();
        int doorsPerWardBase = totalLimit / totalWards;
        int remainder = totalLimit % totalWards;

        int wardIndex = 0;
        for (Ward ward : wards) {
            int doorsForThisWard = doorsPerWardBase;
            if (wardIndex < remainder)
                doorsForThisWard++;
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
