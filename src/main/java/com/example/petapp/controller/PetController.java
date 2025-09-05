package com.example.petapp.controller;

import com.example.petapp.dto.PetDTO;
import com.example.petapp.dto.PetResponseDTO;
import com.example.petapp.model.Pet;
import com.example.petapp.service.PetService;
import com.example.petapp.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    @Autowired
    private PetService petService;

    @PostMapping("/add")
    public ResponseEntity<PetResponseDTO> addPet(@ModelAttribute PetDTO dto) {
        Pet pet = petService.addPet(dto);

        PetResponseDTO response = new PetResponseDTO();
        response.setId(pet.getId());
        response.setUserId(pet.getUserId());
        response.setName(pet.getName());
        response.setBreed(pet.getBreed());
        response.setSex(pet.getSex());
        response.setDob(pet.getDob());
        response.setWeight(pet.getWeight());
        response.setVaccinationStatus(pet.getVaccinationStatus());
        response.setCreatedAt(pet.getCreatedAt());
        response.setNotes(pet.getNotes());
        response.setHealthTags(pet.getHealthTags());
        response.setEmbeddingUpdatedAt(pet.getEmbeddingUpdatedAt());

        if (pet.getProfilePic() != null) {
            response.setProfilePicBase64(ImageUtil.encodeToBase64(pet.getProfilePic()));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PetResponseDTO>> getPets(@PathVariable Long userId) {
        List<Pet> pets = petService.getPetsByUserId(userId);

        List<PetResponseDTO> response = pets.stream().map(pet -> {
            PetResponseDTO dto = new PetResponseDTO();
            dto.setId(pet.getId());
            dto.setUserId(pet.getUserId());
            dto.setName(pet.getName());
            dto.setBreed(pet.getBreed());
            dto.setSex(pet.getSex());
            dto.setDob(pet.getDob());
            dto.setWeight(pet.getWeight());
            dto.setVaccinationStatus(pet.getVaccinationStatus());
            dto.setCreatedAt(pet.getCreatedAt());
            dto.setNotes(pet.getNotes());
            dto.setHealthTags(pet.getHealthTags());
            if (pet.getProfilePic() != null) {
                dto.setProfilePicBase64(ImageUtil.encodeToBase64(pet.getProfilePic()));
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(@PathVariable Long petId) {
        petService.deletePet(petId);
        return ResponseEntity.noContent().build();
    }

}

