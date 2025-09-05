package com.example.petapp.service;

import com.example.petapp.dao.PetDAO;
import com.example.petapp.dto.PetDTO;
import com.example.petapp.model.Pet;
import com.example.petapp.util.EmbeddingClient;
import com.example.petapp.util.VectorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetDAO petDAO;

    @Autowired
    private EmbeddingClient embeddingClient;

    @Override
    public Pet addPet(PetDTO dto) {
        Pet pet = new Pet();
        pet.setUserId(dto.getUserId());
        pet.setName(dto.getName());
        pet.setBreed(dto.getBreed());
        pet.setSex(dto.getSex());
        pet.setDob(dto.getDob());
        pet.setWeight(dto.getWeight());
        pet.setVaccinationStatus(dto.getVaccinationStatus());
        pet.setNotes(dto.getNotes());
        pet.setHealthTags(dto.getHealthTags());

        try {
            if (dto.getProfilePic() != null && !dto.getProfilePic().isEmpty()) {
                pet.setProfilePic(dto.getProfilePic().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading profile picture", e);
        }

        // Build summary string for embedding: Breed + Notes
        String summary = buildEmbeddingText(pet.getBreed(), pet.getNotes());

        // Use your existing EmbeddingClient (returns normalized Double[])
        Double[] vec = embeddingClient.getEmbedding(summary);

        // Convert Double[] to vector literal like "[0.12,0.34,...]"
        String vectorLiteral = VectorUtil.toVectorLiteral(vec);

        // Save pet first (inserts row and returns id)
        Pet saved = petDAO.save(pet);

        // Update embedding after save using existing DAO method
        petDAO.updateEmbedding(saved.getId(), vectorLiteral);

        // Optionally set embeddingUpdatedAt on returned object (mirror DB)
        saved.setEmbeddingUpdatedAt(java.time.LocalDateTime.now());
        saved.setNotes(pet.getNotes());
        saved.setHealthTags(pet.getHealthTags());
        return saved;
    }

    private String buildEmbeddingText(String breed, String notes) {
        StringBuilder sb = new StringBuilder();
        sb.append("Breed: ");
        sb.append(breed == null ? "Unknown" : breed);
        sb.append(". Notes: ");
        if (notes == null || notes.trim().isEmpty()) {
            sb.append("none");
        } else {
            sb.append(notes.trim());
        }
        return sb.toString();
    }

    @Override
    public void deletePet(Long petId) {
        petDAO.delete(petId);
    }

    @Override
    public Pet updatePet(Long petId, PetDTO dto) {
        Pet pet = petDAO.findById(petId);

        pet.setName(dto.getName());
        pet.setBreed(dto.getBreed());
        pet.setSex(dto.getSex());
        pet.setDob(dto.getDob());
        pet.setWeight(dto.getWeight());
        pet.setVaccinationStatus(dto.getVaccinationStatus());
        pet.setNotes(dto.getNotes());
        pet.setHealthTags(dto.getHealthTags());

        try {
            if (dto.getProfilePic() != null && !dto.getProfilePic().isEmpty()) {
                pet.setProfilePic(dto.getProfilePic().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading profile picture", e);
        }

        // rebuild embedding
        String summary = buildEmbeddingText(pet.getBreed(), pet.getNotes());
        Double[] vec = embeddingClient.getEmbedding(summary);
        String vectorLiteral = VectorUtil.toVectorLiteral(vec);

        Pet saved = petDAO.save(pet); // update existing

        petDAO.updateEmbedding(saved.getId(), vectorLiteral);
        saved.setEmbeddingUpdatedAt(java.time.LocalDateTime.now());
        return saved;
    }


    @Override
    public List<Pet> getPetsByUserId(Long userId) {
        return petDAO.findByUserId(userId);
    }
}
