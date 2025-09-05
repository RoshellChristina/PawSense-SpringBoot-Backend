package com.example.petapp.service;

import com.example.petapp.dto.PetDTO;
import com.example.petapp.model.Pet;

import java.util.List;

public interface PetService {
    Pet addPet(PetDTO dto);

    void deletePet(Long petId);

    Pet updatePet(Long petId, PetDTO dto);

    List<Pet> getPetsByUserId(Long userId);
}
