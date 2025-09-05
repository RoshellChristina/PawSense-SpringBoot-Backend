package com.example.petapp.dao;

import com.example.petapp.model.PetSymptomPrediction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class PetPredictionDAOImpl implements PetPredictionDAO {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public PetSymptomPrediction save(PetSymptomPrediction p) {
        // Using PostgreSQL RETURNING to get id and created_at
        String sql = "INSERT INTO pet_symptoms " +
                "(pet_id, user_id, symptoms, disease_duration_days, prediction, prediction_prob) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id, created_at";

        return jdbc.queryForObject(sql, new Object[] {
                p.getPetId(),
                p.getUserId(),
                p.getSymptoms(),
                p.getDiseaseDurationDays(),
                p.getPrediction(),
                p.getPredictionProb()
        }, (rs, rowNum) -> {
            p.setId(rs.getLong("id"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                p.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
            }
            return p;
        });
    }

    @Override
    public List<PetSymptomPrediction> findByPetId(Long petId) {
        String sql = "SELECT * FROM pet_symptoms WHERE pet_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, new Object[]{petId}, predictionRowMapper);
    }

    private final RowMapper<PetSymptomPrediction> predictionRowMapper = (rs, rn) -> {
        PetSymptomPrediction p = new PetSymptomPrediction();
        p.setId(rs.getLong("id"));
        p.setPetId(rs.getLong("pet_id"));
        p.setUserId(rs.getObject("user_id") != null ? rs.getLong("user_id") : null);
        p.setSymptoms(rs.getString("symptoms"));
        p.setDiseaseDurationDays(rs.getObject("disease_duration_days") != null ? rs.getInt("disease_duration_days") : null);
        p.setPrediction(rs.getString("prediction"));
        p.setPredictionProb(rs.getObject("prediction_prob") != null ? rs.getDouble("prediction_prob") : null);
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        }
        return p;
    };

    @Override
    public List<PetSymptomPrediction> findByUserId(Long userId) {
        String sql = "SELECT * FROM pet_symptoms WHERE user_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, new Object[]{userId}, predictionRowMapper);
    }

}
