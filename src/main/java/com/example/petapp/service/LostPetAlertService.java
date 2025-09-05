// src/main/java/com/example/petapp/service/LostPetAlertService.java
package com.example.petapp.service;
import com.example.petapp.dto.LostPetAlertDTO;
import com.example.petapp.model.LostPetAlert;

import java.util.Optional;

public interface LostPetAlertService {
    LostPetAlertDTO create(LostPetAlertDTO dto);
    LostPetAlertDTO get(Long id);
    boolean update(LostPetAlertDTO dto);
    boolean delete(Long id);
    Optional<LostPetAlert> getById(Long id);
}
