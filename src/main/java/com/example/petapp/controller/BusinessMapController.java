// src/main/java/com/example/petapp/controller/BusinessMapController.java
package com.example.petapp.controller;

import com.example.petapp.dto.BusinessAddressDTO;
import com.example.petapp.model.Address;
import com.example.petapp.service.AddressService;
import com.example.petapp.service.BusinessService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map")
public class BusinessMapController {


    private final BusinessService businessService;
    public BusinessMapController(BusinessService businessService) {
        this.businessService = businessService;
    }

    // GET /api/map/nearby?userId=123
    @GetMapping("/nearby")
    public List<Address> getNearbyBusinesses(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false, defaultValue = "10") double radiusKm) {
        return businessService.getNearbyBusinesses(lon, lat, radiusKm);
    }

    // **New endpoint that includes fullname and business type**
    @GetMapping("/nearby-with-type")
    public List<BusinessAddressDTO> getNearbyBusinessesWithType(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false, defaultValue = "10") double radiusKm) {
        return businessService.getNearbyBusinessesWithType(lon, lat, radiusKm);
    }

}
