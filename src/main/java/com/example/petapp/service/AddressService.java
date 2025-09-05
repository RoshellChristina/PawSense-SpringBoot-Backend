// src/main/java/com/example/petapp/service/AddressService.java
package com.example.petapp.service;

import com.example.petapp.dao.AddressDao;
import com.example.petapp.model.Address;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressDao addressDao;

    public AddressService(AddressDao addressDao) {
        this.addressDao = addressDao;
    }

    public List<Address> findNearbyBusinesses(Long userId) {
        return addressDao.findNearbyBusinesses(userId);
    }
}
