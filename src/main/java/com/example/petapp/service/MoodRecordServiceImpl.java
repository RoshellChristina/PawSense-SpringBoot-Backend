package com.example.petapp.service;

import com.example.petapp.dao.MoodRecordDao;
import com.example.petapp.dto.MoodRecordDto;
import com.example.petapp.model.MoodRecord;
import com.example.petapp.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MoodRecordServiceImpl implements MoodRecordService {

    private final MoodRecordDao dao;

    @Autowired
    public MoodRecordServiceImpl(MoodRecordDao dao) {
        this.dao = dao;
    }

    @Override
    public void recordMood(MoodRecordDto dto) {
        MoodRecord record = new MoodRecord();
        record.setId(dto.getId());
        record.setPetId(dto.getPetId());
        record.setEmotion(dto.getEmotion());
        record.setConfidence(dto.getConfidence());
        if (dto.getImageBase64() != null) {
            record.setImage(ImageUtil.decodeFromBase64(dto.getImageBase64()));
        }
        dao.save(record);
    }

    @Override
    public MoodRecord getMoodRecord(Long id) {
        return dao.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }

    @Override
    public List<MoodRecord> getByPetId(Long petId) {
        return dao.findByPetId(petId);
    }

    @Override
    public void deleteMoodRecord(Long id) {
        MoodRecord record = dao.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));
        dao.delete(record.getId()); // ✅ pass the ID
    }



}