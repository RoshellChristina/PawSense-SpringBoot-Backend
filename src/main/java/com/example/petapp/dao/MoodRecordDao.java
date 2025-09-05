package com.example.petapp.dao;

import com.example.petapp.model.MoodRecord;
import java.util.List;
import java.util.Optional;

public interface MoodRecordDao {
    int save(MoodRecord moodRecord);
    Optional<MoodRecord> findById(Long id);
    List<MoodRecord> findByPetId(Long petId);

    int delete(Long id);
}