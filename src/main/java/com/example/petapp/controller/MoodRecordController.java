package com.example.petapp.controller;

import com.example.petapp.dto.MoodRecordDto;
import com.example.petapp.model.MoodRecord;
import com.example.petapp.service.MoodRecordService;
import com.example.petapp.service.PetAdviceService;
import com.example.petapp.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/moods")
public class MoodRecordController {

    private final MoodRecordService moodService;
    private final PetAdviceService adviceService;

    @Autowired
    public MoodRecordController(MoodRecordService moodService,
                                PetAdviceService adviceService) {
        this.moodService = moodService;
        this.adviceService = adviceService;
    }

    @PostMapping("/record")
    public ResponseEntity<?> recordMood(@RequestBody MoodRecordDto dto) {
        if (dto.getPetId() == null
                || dto.getEmotion() == null
                || dto.getConfidence() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Invalid payload"));
        }

        // Persist the mood record
        moodService.recordMood(dto);

        // Generate advice
        String advice = adviceService.generateAdvice(dto.getEmotion());

        // Return both confirmation and advice
        Map<String,String> response = new HashMap<>();
        response.put("message", "Mood recorded");
        response.put("advice", advice);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoodRecordDto> getRecord(@PathVariable Long id) {
        MoodRecord record = moodService.getMoodRecord(id);
        MoodRecordDto dto = new MoodRecordDto();
        dto.setPetId(record.getPetId());
        dto.setEmotion(record.getEmotion());
        dto.setConfidence(record.getConfidence());
        dto.setImageBase64(ImageUtil.encodeToBase64(record.getImage()));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<MoodRecordDto>> getByPet(@PathVariable Long petId) {
        List<MoodRecord> records = moodService.getByPetId(petId);
        List<MoodRecordDto> dtos = records.stream().map(rec -> {
            MoodRecordDto d = new MoodRecordDto();
            d.setId(rec.getId());
            d.setPetId(rec.getPetId());
            d.setEmotion(rec.getEmotion());
            d.setConfidence(rec.getConfidence());
            d.setImageBase64(ImageUtil.encodeToBase64(rec.getImage()));
            return d;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        MoodRecord record = moodService.getMoodRecord(id);
        return ResponseEntity.ok(record.getImage());
    }

    @GetMapping("/advice/{emotion}")
    public ResponseEntity<Map<String,String>> getAdvice(@PathVariable String emotion) {
        String advice = adviceService.generateAdvice(emotion);
        return ResponseEntity.ok(Map.of("advice", advice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMoodRecord(@PathVariable Long id) {
        try {
            moodService.deleteMoodRecord(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}

