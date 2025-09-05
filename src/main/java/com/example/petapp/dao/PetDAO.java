package com.example.petapp.dao;

import com.example.petapp.model.Pet;
import java.util.List;

public interface PetDAO {
    Pet save(Pet pet);
    List<Pet> findByUserId(Long userId);
    Pet findById(Long id);
    // update embedding for an existing pet (embedding as vector literal string)
    void updateEmbedding(Long petId, String vectorLiteral);

    void delete(Long id);
}

