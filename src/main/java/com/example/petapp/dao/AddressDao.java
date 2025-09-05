// src/main/java/com/example/petapp/dao/AddressDao.java
package com.example.petapp.dao;

import com.example.petapp.model.Address;
import java.util.List;

public interface AddressDao {
    List<Address> findByUserId(Long userId);
    void save(Address address);
    void update(Address address);
    void delete(Long addressId);

    List<Address> findNearbyBusinesses(Long userId);
}
