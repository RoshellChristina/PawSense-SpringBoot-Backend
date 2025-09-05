package com.example.petapp.service;

import com.example.petapp.dao.ContentDao;
import com.example.petapp.model.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private final ContentDao contentDao;

    @Autowired
    public RecommendationService(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    /**
     * Recommend content for a pet.
     *
     * @param petId pet id
     * @param businessOnly filter for business contents authored by business users
     * @param requestingUserId id of calling user (used to exclude their own content)
     * @param limit number of results
     * @return list of recommended content
     */
    public List<Content> recommendForPet(Long petId, boolean businessOnly, Long requestingUserId, int limit) {
        return contentDao.recommendForPet(petId, businessOnly, requestingUserId, limit);
    }
}
