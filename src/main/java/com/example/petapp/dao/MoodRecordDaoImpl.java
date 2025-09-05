package com.example.petapp.dao;

import com.example.petapp.model.MoodRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class MoodRecordDaoImpl implements MoodRecordDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MoodRecordDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(MoodRecord moodRecord) {
        String sql = "INSERT INTO mood_records (pet_id, emotion, confidence, image) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                moodRecord.getPetId(),
                moodRecord.getEmotion(),
                moodRecord.getConfidence(),
                moodRecord.getImage()
        );
    }

    @Override
    public Optional<MoodRecord> findById(Long id) {
        String sql = "SELECT id, pet_id, emotion, confidence, image, recorded_at FROM mood_records WHERE id = ?";
        MoodRecord record = jdbcTemplate.queryForObject(sql, new Object[]{id}, new MoodRowMapper());
        return Optional.ofNullable(record);
    }

    @Override
    public List<MoodRecord> findByPetId(Long petId) {
        String sql = "SELECT id, pet_id, emotion, confidence, image, recorded_at FROM mood_records WHERE pet_id = ? ORDER BY recorded_at DESC";
        return jdbcTemplate.query(sql, new Object[]{petId}, new MoodRowMapper());
    }

    private static class MoodRowMapper implements RowMapper<MoodRecord> {
        @Override
        public MoodRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            MoodRecord m = new MoodRecord();
            m.setId(rs.getLong("id"));
            m.setPetId(rs.getLong("pet_id"));
            m.setEmotion(rs.getString("emotion"));
            m.setConfidence(rs.getDouble("confidence"));
            m.setImage(rs.getBytes("image"));
            m.setRecordedAt(rs.getTimestamp("recorded_at").toLocalDateTime());
            return m;
        }
    }

    @Override
    public int delete(Long id) {
        String sql = "DELETE FROM mood_records WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

}


