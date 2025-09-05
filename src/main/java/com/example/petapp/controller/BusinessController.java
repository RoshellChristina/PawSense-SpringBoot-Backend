package com.example.petapp.controller;

import com.example.petapp.model.Address;
import com.example.petapp.service.BusinessService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // Get nearby businesses
    @GetMapping("/nearby")
    public List<Address> getNearbyBusinesses(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false, defaultValue = "5") double radiusKm) {

        return businessService.getNearbyBusinesses(lon, lat, radiusKm);
    }
}
