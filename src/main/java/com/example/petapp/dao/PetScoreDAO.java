package com.example.petapp.dao;

import com.example.petapp.model.PetScoreRequest;
import com.example.petapp.model.PetScoreResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class PetScoreDAO {

    @Autowired
    private JdbcTemplate jdbc;

    public PetScoreResponse savePrediction(PetScoreRequest req, Double totalScore) {
        String sql = "INSERT INTO pet_score_predictions " +
                "(pet_id, hurt, hunger, hydration, hygiene, happiness, mobility, mgd, total_score, predicted_at, " +
                "household_env, sleep_hours) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, predicted_at";
        return jdbc.queryForObject(sql, new Object[] {
                req.getPetId(),
                req.getHurt(),
                req.getHunger(),
                req.getHydration(),
                req.getHygiene(),
                req.getHappiness(),
                req.getMobility(),
                req.getMgd(),
                totalScore,
                new Timestamp(System.currentTimeMillis()),
                req.getHouseholdEnv(),
                req.getSleepHours()
        }, (rs, rowNum) -> {
            PetScoreResponse resp = new PetScoreResponse();
            resp.setId(rs.getLong("id"));
            resp.setPetId(req.getPetId());
            resp.setTotalScore(totalScore);
            resp.setHurt(req.getHurt());
            resp.setHunger(req.getHunger());
            resp.setHydration(req.getHydration());
            resp.setHygiene(req.getHygiene());
            resp.setHappiness(req.getHappiness());
            resp.setMobility(req.getMobility());
            resp.setMgd(req.getMgd());
            resp.setHouseholdEnv(req.getHouseholdEnv());
            resp.setSleepHours(req.getSleepHours());
            resp.setPredictedAt(rs.getTimestamp("predicted_at").toLocalDateTime());
            return resp;
        });
    }

    public List<PetScoreResponse> getHistory(Long petId) {
        String sql = "SELECT * FROM pet_score_predictions WHERE pet_id = ? ORDER BY predicted_at DESC";
        return jdbc.query(sql, new Object[]{petId}, (rs, rowNum) -> {
            PetScoreResponse resp = new PetScoreResponse();
            resp.setId(rs.getLong("id"));
            resp.setPetId(rs.getLong("pet_id"));
            resp.setTotalScore(rs.getDouble("total_score"));
            resp.setHurt(rs.getDouble("hurt"));
            resp.setHunger(rs.getDouble("hunger"));
            resp.setHydration(rs.getDouble("hydration"));
            resp.setHygiene(rs.getDouble("hygiene"));
            resp.setHappiness(rs.getDouble("happiness"));
            resp.setMobility(rs.getDouble("mobility"));
            resp.setMgd(rs.getDouble("mgd"));
            resp.setHouseholdEnv(rs.getString("household_env"));
            resp.setSleepHours(rs.getDouble("sleep_hours"));
            resp.setPredictedAt(rs.getTimestamp("predicted_at").toLocalDateTime());
            return resp;
        });
    }
}
