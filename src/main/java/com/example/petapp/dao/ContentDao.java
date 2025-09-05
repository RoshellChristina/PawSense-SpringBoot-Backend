package com.example.petapp.dao;

import com.example.petapp.model.Content;

import java.util.List;
import java.util.Optional;

public interface ContentDao {
    Content create(Content content);
    Optional<Content> findById(Long id);
    List<Content> findAll();
    boolean update(Content content);
    boolean deleteById(Long id);
    List<Content> findByUserId(Long userId);

    /**
     * Recommend content for a pet.
     *
     * @param petId ID of the pet whose embedding/tags will be used
     * @param businessOnly if true, only recommend content of type product/service AND authored by users with role='business'
     * @param requestingUserId the id of the user asking (used to exclude same user's content)
     * @param limit max results
     * @return list of content
     */
    List<Content> recommendForPet(Long petId, boolean businessOnly, Long requestingUserId, int limit);
}
