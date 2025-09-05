// src/main/java/com/example/petapp/dao/LostPetAlertDao.java
package com.example.petapp.dao;

import com.example.petapp.model.LostPetAlert;
import java.util.Optional;

public interface LostPetAlertDao {
    LostPetAlert create(LostPetAlert alert);
    Optional<LostPetAlert> findById(Long id);
    boolean update(LostPetAlert alert);
    boolean deleteById(Long id);
}
