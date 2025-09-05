package com.example.petapp.service;

import com.example.petapp.dao.BusinessDao;
import com.example.petapp.dto.BusinessAddressDTO;
import com.example.petapp.model.Address;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class
BusinessServiceImpl implements BusinessService {
    private final BusinessDao dao;

    public BusinessServiceImpl(BusinessDao dao) {
        this.dao = dao;
    }

    @Override
    public List<Address> getNearbyBusinesses(double lon, double lat, double radiusKm) {
        return dao.findNearbyBusinesses(lon, lat, radiusKm);
    }
    @Override
    public List<BusinessAddressDTO> getNearbyBusinessesWithType(double lon, double lat, double radiusKm) {
        return dao.findNearbyBusinessesWithType(lon, lat, radiusKm);
    }
}
