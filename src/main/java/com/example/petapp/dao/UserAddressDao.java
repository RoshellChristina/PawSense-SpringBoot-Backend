// src/main/java/com/example/petapp/dao/UserAddressDao.java
package com.example.petapp.dao;

import java.util.List;

public interface UserAddressDao {
    List<Long> findUserIdsWithinRadius(double lat, double lng, double radiusKm);
}
