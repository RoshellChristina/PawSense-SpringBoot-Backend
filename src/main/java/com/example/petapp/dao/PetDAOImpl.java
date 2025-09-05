package com.example.petapp.dao;

import com.example.petapp.model.Pet;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class PetDAOImpl implements PetDAO {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public Pet save(Pet pet) {

        String embeddingLiteral = null;

        String sql = "INSERT INTO pets (user_id, name, breed, sex, dob, weight, vaccination_status, profile_pic, notes, health_tags, embedding, embedding_updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?::vector, ?) RETURNING id, created_at";


        Connection conn = null;
        Array sqlArray = null;
        try {
            conn = Objects.requireNonNull(jdbc.getDataSource()).getConnection();

            // prepare health_tags array (may be null)
            if (pet.getHealthTags() != null && !pet.getHealthTags().isEmpty()) {
                String[] arr = pet.getHealthTags().toArray(new String[0]);
                sqlArray = conn.createArrayOf("text", arr);
            }

            Object embeddingObj = getObject();

            return jdbc.queryForObject(sql, new Object[] {
                pet.getUserId(),
                pet.getName(),
                pet.getBreed(),
                pet.getSex(),
                pet.getDob(),
                pet.getWeight(),
                pet.getVaccinationStatus(),
                pet.getProfilePic(),
                pet.getNotes(),
                sqlArray,
                embeddingObj,
                null
        }, (rs, rowNum) -> {
            pet.setId(rs.getLong("id"));
            pet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return pet;
        });
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting pet", e);
        } finally {
            try { if (sqlArray != null) sqlArray.free(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    @Nullable
    private static Object getObject() {
        Object embeddingObj = null;
        // service should have set an embedding string in the pet.notes or elsewhere; here we expect pet.getNotes() unchanged.
        // If you prefer to pass embedding from service directly, modify this method signature.
        // For now we assume caller put embedding as the 'notes' field? No — instead we'll leave embedding null if not present.
        // Ideally, call pet.setEmbeddingLiteral(...) or pass embedding param.
        // We'll check if pet.getNotes() starts with "[": not reliable. So embedding will be updated separately using updateEmbedding() after save.
        embeddingObj = null;
        return embeddingObj;
    }

    @Override
    public List<Pet> findByUserId(Long userId) {
        String sql = "SELECT * FROM pets WHERE user_id = ?";
        return jdbc.query(sql, new Object[]{userId}, (rs, rowNum) -> {
            Pet pet = new Pet();
            pet.setId(rs.getLong("id"));
            pet.setUserId(rs.getLong("user_id"));
            pet.setName(rs.getString("name"));
            pet.setBreed(rs.getString("breed"));
            pet.setSex(rs.getString("sex"));
            Date dobDate = rs.getDate("dob");
            pet.setDob(dobDate != null ? dobDate.toLocalDate() : null);
            pet.setWeight(rs.getDouble("weight"));
            pet.setVaccinationStatus(rs.getString("vaccination_status"));
            pet.setProfilePic(rs.getBytes("profile_pic"));
            pet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            pet.setNotes(rs.getString("notes"));
            // health_tags
            Array arr = rs.getArray("health_tags");
            if (arr != null) {
                try {
                    Object[] objs = (Object[]) arr.getArray();
                    List<String> tags = new ArrayList<>();
                    for (Object o : objs) tags.add((String) o);
                    pet.setHealthTags(tags);
                } finally {
                    try { arr.free(); } catch (Exception ignored) {}
                }
            }
            Timestamp embTs = rs.getTimestamp("embedding_updated_at");
            if (embTs != null) pet.setEmbeddingUpdatedAt(embTs.toLocalDateTime());
            return pet;
        });
    }

    @Override
    public Pet findById(Long id) {
        String sql = "SELECT * FROM pets WHERE id = ?";
        return jdbc.queryForObject(sql, new PetRowMapper(), id);
    }

    @Override
    public void updateEmbedding(Long petId, String vectorLiteral) {
        // vectorLiteral should be like: "[0.12,0.34, ...]"
        String sql = "UPDATE pets SET embedding = ?::vector, embedding_updated_at = now() WHERE id = ?";
        jdbc.update(sql, vectorLiteral, petId);
    }


    private static final class PetRowMapper implements RowMapper<Pet> {
        @Override
        public Pet mapRow(ResultSet rs, int rowNum) throws SQLException {
            Pet pet = new Pet();
            pet.setId(rs.getLong("id"));
            pet.setUserId(rs.getLong("user_id"));
            pet.setName(rs.getString("name"));
            pet.setBreed(rs.getString("breed"));
            pet.setSex(rs.getString("sex"));

            java.sql.Date sqlDob = rs.getDate("dob");
            pet.setDob(sqlDob != null ? sqlDob.toLocalDate() : null);

            pet.setWeight(rs.getDouble("weight"));
            pet.setVaccinationStatus(rs.getString("vaccination_status"));
            pet.setProfilePic(rs.getBytes("profile_pic"));
            pet.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            pet.setNotes(rs.getString("notes"));
            Array arr = rs.getArray("health_tags");
            if (arr != null) {
                try {
                    Object[] objs = (Object[]) arr.getArray();
                    List<String> tags = new ArrayList<>();
                    for (Object o : objs) tags.add((String) o);
                    pet.setHealthTags(tags);
                } finally {
                    try { arr.free(); } catch (Exception ignored) {}
                }
            }
            Timestamp embTs = rs.getTimestamp("embedding_updated_at");
            if (embTs != null) pet.setEmbeddingUpdatedAt(embTs.toLocalDateTime());
            return pet;
        }
    }
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM pets WHERE id = ?";
        jdbc.update(sql, id);
    }

}
