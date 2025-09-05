package com.example.petapp.dao;

import com.example.petapp.dto.BusinessAddressDTO;
import com.example.petapp.model.Address;
import java.util.List;

public interface BusinessDao {
    List<Address> findNearbyBusinesses(double lon, double lat, double radiusKm);

    List<BusinessAddressDTO> findNearbyBusinessesWithType(double lon, double lat, double radiusKm);
}
