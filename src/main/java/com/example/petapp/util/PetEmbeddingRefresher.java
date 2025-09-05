package com.example.petapp.util;

import com.example.petapp.dao.PetDAO;
import com.example.petapp.model.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetEmbeddingRefresher {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EmbeddingClient embeddingClient; // your existing util client returning Double[]

    @Autowired
    private PetDAO petDAO;

    /**
     * Run once a day at 03:00 (server time). Adjust the cron expression as you prefer.
     * This job finds pets with missing embeddings or older than 30 days and refreshes them.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshEmbeddings() {
        String sql = "SELECT id, breed, notes FROM pets WHERE embedding IS NULL OR embedding_updated_at < now() - INTERVAL '30 days' LIMIT 200";
        List<Pet> list = jdbc.query(sql, (rs, rowNum) -> {
            Pet p = new Pet();
            p.setId(rs.getLong("id"));
            p.setBreed(rs.getString("breed"));
            p.setNotes(rs.getString("notes"));
            return p;
        });

        for (Pet p : list) {
            try {
                String summary = "Breed: " + (p.getBreed() == null ? "Unknown" : p.getBreed()) +
                        ". Notes: " + (p.getNotes() == null ? "none" : p.getNotes());

                // Use your existing client that returns normalized Double[]
                Double[] vec = embeddingClient.getEmbedding(summary);

                // Defensive check
                if (vec == null) {
                    System.err.println("Embedding service returned null for pet " + p.getId());
                    continue;
                }

                String vectorLiteral = VectorUtil.toVectorLiteral(vec);
                petDAO.updateEmbedding(p.getId(), vectorLiteral);
            } catch (Exception ex) {
                // log and continue
                System.err.println("Failed to refresh embedding for pet " + p.getId() + ": " + ex.getMessage());
            }
        }
    }
}
