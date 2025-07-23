package com.techpool.muncipality.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.techpool.muncipality.services.DoorGenerationService;

@RestController
@RequestMapping("/api/generate-doors")
public class DoorGenController {
    @Autowired
    DoorGenerationService service;

    @GetMapping
    public List<Map<String, Object>> generate(@RequestParam(defaultValue = "6000") int limit) {
        return service.generate(limit); // Now, only limit
    }
}
