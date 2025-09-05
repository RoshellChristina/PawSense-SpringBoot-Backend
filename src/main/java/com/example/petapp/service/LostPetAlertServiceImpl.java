// src/main/java/com/example/petapp/service/LostPetAlertServiceImpl.java
package com.example.petapp.service;
import com.example.petapp.dao.LostPetAlertDao;
import com.example.petapp.dto.LostPetAlertDTO;
import com.example.petapp.model.LostPetAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LostPetAlertServiceImpl implements LostPetAlertService {
    @Autowired private LostPetAlertDao dao;

    @Override
    public LostPetAlertDTO create(LostPetAlertDTO dto) {
        LostPetAlert a = new LostPetAlert();
        a.setLatitude(dto.latitude);
        a.setLongitude(dto.longitude);
        a.setLastSeenAddress(dto.lastSeenAddress);
        a.setRadiusKm(dto.radiusKm);
        LostPetAlert saved = dao.create(a);
        LostPetAlertDTO out = new LostPetAlertDTO();
        out.id = saved.getId();
        out.latitude = saved.getLatitude();
        out.longitude = saved.getLongitude();
        out.lastSeenAddress = saved.getLastSeenAddress();
        out.radiusKm = saved.getRadiusKm();
        out.createdAt = saved.getCreatedAt();
        return out;
    }

    @Override
    public LostPetAlertDTO get(Long id) {
        return dao.findById(id).map(saved -> {
            LostPetAlertDTO o = new LostPetAlertDTO();
            o.id = saved.getId();
            o.latitude = saved.getLatitude();
            o.longitude = saved.getLongitude();
            o.lastSeenAddress = saved.getLastSeenAddress();
            o.radiusKm = saved.getRadiusKm();
            o.createdAt = saved.getCreatedAt();
            return o;
        }).orElse(null);
    }

    @Override
    public boolean update(LostPetAlertDTO dto) {
        LostPetAlert a = new LostPetAlert();
        a.setId(dto.id);
        a.setLatitude(dto.latitude);
        a.setLongitude(dto.longitude);
        a.setLastSeenAddress(dto.lastSeenAddress);
        a.setRadiusKm(dto.radiusKm);
        return dao.update(a);
    }

    @Override
    public Optional<LostPetAlert> getById(Long id) {
        // Direct pass-through to DAO so callers can access raw model fields
        return dao.findById(id);
    }

    @Override
    public boolean delete(Long id) {
        return dao.deleteById(id);
    }
}
