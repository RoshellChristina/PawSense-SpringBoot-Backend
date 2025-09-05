package com.example.petapp.service;

import com.example.petapp.dto.BusinessAddressDTO;
import com.example.petapp.model.Address;
import java.util.List;

public interface BusinessService {
    List<Address> getNearbyBusinesses(double lon, double lat, double radiusKm);
    List<BusinessAddressDTO> getNearbyBusinessesWithType(double lon, double lat, double radiusKm);
}
