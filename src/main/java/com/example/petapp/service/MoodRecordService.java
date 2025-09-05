package com.example.petapp.service;

import com.example.petapp.dto.MoodRecordDto;
import com.example.petapp.model.MoodRecord;
import java.util.List;

public interface MoodRecordService {
    void recordMood(MoodRecordDto dto);
    MoodRecord getMoodRecord(Long id);
    List<MoodRecord> getByPetId(Long petId);

    void deleteMoodRecord(Long id);
}