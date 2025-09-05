package com.example.petapp.dao;

import com.example.petapp.model.Content;
import com.example.petapp.model.User;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class ContentDaoImpl implements ContentDao {

    private final JdbcTemplate jdbc;

    @Autowired
    public ContentDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Content create(Content c) {
        final String sql = """
            INSERT INTO content
              (user_id, content_type, lost_pet_alert_id, title, body, tags,
               media1, media1_type, media2, media2_type, media3, media3_type,
               media4, media4_type, media5, media5_type, embedding)
            VALUES (?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?, ?, ?,
                    ?, ?, ?, ?,?)
            """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, c.getUserId(), Types.BIGINT);
            ps.setString(2, c.getContentType());
            ps.setObject(3, c.getLostPetAlertId(), Types.BIGINT);
            ps.setString(4, c.getTitle());
            ps.setString(5, c.getBody());
            if (c.getTags() != null) {
                ps.setArray(6, conn.createArrayOf("VARCHAR", c.getTags().toArray()));
            } else ps.setNull(6, Types.ARRAY);

            ps.setBytes(7, c.getMedia1());
            ps.setString(8, c.getMedia1Type());
            ps.setBytes(9, c.getMedia2());
            ps.setString(10, c.getMedia2Type());
            ps.setBytes(11, c.getMedia3());
            ps.setString(12, c.getMedia3Type());
            ps.setBytes(13, c.getMedia4());
            ps.setString(14, c.getMedia4Type());
            ps.setBytes(15, c.getMedia5());
            ps.setString(16, c.getMedia5Type());

            // embedding -> PGobject (pgvector)
            try {
                if (c.getEmbedding() != null) {
                    PGobject pg = toPgVector(c.getEmbedding());
                    ps.setObject(17, pg);
                } else {
                    ps.setNull(17, Types.OTHER);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to set embedding PGobject", e);
            }

            return ps;

        }, kh);

        Long id = kh.getKey().longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public Optional<Content> findById(Long id) {
        final String sql = """
            SELECT c.*, u.id AS u_id, u.username, u.profile_picture, u.full_name, u.bio
              FROM content c JOIN users u ON c.user_id = u.id
             WHERE c.id = ?
            """;
        return jdbc.query(sql, new ContentRowMapper(), id)
                .stream().findFirst();
    }

    @Override
    public List<Content> findAll() {
        final String sql = """
            SELECT c.*, u.id AS u_id, u.username, u.profile_picture, u.full_name, u.bio
              FROM content c JOIN users u ON c.user_id = u.id
             ORDER BY c.created_at DESC
            """;
        return jdbc.query(sql, new ContentRowMapper());
    }

    @Override
    public List<Content> findByUserId(Long userId) {
        final String sql = """
        SELECT c.*, u.id AS u_id, u.username, u.profile_picture, u.full_name, u.bio
          FROM content c JOIN users u ON c.user_id = u.id
         WHERE u.id = ?
         ORDER BY c.created_at DESC
        """;
        return jdbc.query(sql, new ContentRowMapper(), userId);
    }


    @Override
    public boolean update(Content c) {
        final String sql = """
            UPDATE content SET
              user_id=?, content_type=?, lost_pet_alert_id=?, title=?, body=?, tags=?,
              media1=?, media1_type=?, media2=?, media2_type=?, media3=?, media3_type=?,
              media4=?, media4_type=?, media5=?, media5_type=?, embedding=?
            WHERE id=?
            """;
        return jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, c.getUserId(), Types.BIGINT);
            ps.setString(2, c.getContentType());
            ps.setObject(3, c.getLostPetAlertId(), Types.BIGINT);
            ps.setString(4, c.getTitle());
            ps.setString(5, c.getBody());
            if (c.getTags() != null) {
                ps.setArray(6, conn.createArrayOf("VARCHAR", c.getTags().toArray()));
            } else ps.setNull(6, Types.ARRAY);

            ps.setBytes(7, c.getMedia1());
            ps.setString(8, c.getMedia1Type());
            ps.setBytes(9, c.getMedia2());
            ps.setString(10, c.getMedia2Type());
            ps.setBytes(11, c.getMedia3());
            ps.setString(12, c.getMedia3Type());
            ps.setBytes(13, c.getMedia4());
            ps.setString(14, c.getMedia4Type());
            ps.setBytes(15, c.getMedia5());
            ps.setString(16, c.getMedia5Type());

            try {
                if (c.getEmbedding() != null) {
                    PGobject pg = toPgVector(c.getEmbedding());
                    ps.setObject(17, pg);
                } else {
                    ps.setNull(17, Types.OTHER);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to set embedding PGobject", e);
            }
            ps.setLong(18, c.getId());
            return ps;
        }) == 1;
    }

    @Override
    public boolean deleteById(Long id) {
        return jdbc.update("DELETE FROM content WHERE id = ?", id) == 1;
    }

    private static class ContentRowMapper implements RowMapper<Content> {
        @Override
        public Content mapRow(ResultSet rs, int rowNum) throws SQLException {
            Content c = new Content();
            c.setId(rs.getLong("id"));
            c.setUserId(rs.getLong("user_id"));
            c.setContentType(rs.getString("content_type"));
            c.setLostPetAlertId(rs.getLong("lost_pet_alert_id"));
            c.setTitle(rs.getString("title"));
            c.setBody(rs.getString("body"));

            Array arr = rs.getArray("tags");
            if (arr != null) c.setTags(List.of((String[])arr.getArray()));

            c.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));

            c.setMedia1(rs.getBytes("media1"));      c.setMedia1Type(rs.getString("media1_type"));
            c.setMedia2(rs.getBytes("media2"));      c.setMedia2Type(rs.getString("media2_type"));
            c.setMedia3(rs.getBytes("media3"));      c.setMedia3Type(rs.getString("media3_type"));
            c.setMedia4(rs.getBytes("media4"));      c.setMedia4Type(rs.getString("media4_type"));
            c.setMedia5(rs.getBytes("media5"));      c.setMedia5Type(rs.getString("media5_type"));

            User u = new User();
            u.setId(rs.getLong("u_id"));
            u.setUsername(rs.getString("username"));
            u.setProfilePicture(rs.getBytes("profile_picture"));
            u.setFullname(rs.getString("full_name"));
            u.setBio(rs.getString("bio"));
            try { u.setRole(rs.getString("role")); } catch (Exception ignored) {}
            c.setUser(u);

            // read optional distance if present (some queries add it)
            try {
                double d = rs.getDouble("distance");
                if (!rs.wasNull()) c.setDistance(d);
            } catch (SQLException ignored) {}


            // Read pgvector column as string and parse to Double[]
            String embeddingStr = null;
            try {
                embeddingStr = rs.getString("embedding"); // e.g. "[0.123,-0.456,...]"
            } catch (SQLException ignore) {
                embeddingStr = null;
            }

            if (embeddingStr != null) {
                String trimmed = embeddingStr.trim();
                if (trimmed.startsWith("[")) trimmed = trimmed.substring(1);
                if (trimmed.endsWith("]")) trimmed = trimmed.substring(0, trimmed.length() - 1);

                if (trimmed.isEmpty()) {
                    c.setEmbedding(null);
                } else {
                    String[] parts = trimmed.split(",");
                    Double[] emb = new Double[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        String p = parts[i].trim();
                        if (p.isEmpty()) emb[i] = 0.0;
                        else emb[i] = Double.parseDouble(p);
                    }
                    c.setEmbedding(emb);
                }
            } else {
                c.setEmbedding(null);
            }

            // prepare base64 for JSON
            c.encodeMediaToBase64();
            return c;
        }
    }

    /** Build a PGobject with type vector from Double[] */
    private PGobject toPgVector(Double[] emb) throws SQLException {
        if (emb == null) return null;
        // join values with comma, ensure locale independent conversion
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < emb.length; i++) {
            if (i > 0) sb.append(",");
            Double v = emb[i] == null ? 0.0 : emb[i];
            sb.append(v.toString());
        }
        sb.append("]");
        PGobject pg = new PGobject();
        pg.setType("vector");
        pg.setValue(sb.toString());
        return pg;
    }

    @Override
    public List<Content> recommendForPet(Long petId, boolean businessOnly, Long requestingUserId, int limit) {
        // types filter
        String[] businessTypes = new String[] { "product", "service" };
        String[] defaultTypes  = new String[] { "user_post", "lost_pet_alert", "event" };
        String[] types = businessOnly ? businessTypes : defaultTypes;

        /*
         * Build final vector SQL string (must be effectively final for lambda capture).
         * Parameter order:
         * 1 = petId (used in CROSS JOIN subselect)
         * 2 = content types array
         * 3 = requestingUserId (used to exclude same user's content)
         * 4 = limit
         */
        StringBuilder vecSb = new StringBuilder();
        vecSb.append("SELECT c.*, u.id AS u_id, u.username, u.profile_picture, u.full_name, u.bio, u.role, ");
        vecSb.append("(c.embedding <=> p.embedding) AS distance ");
        vecSb.append("FROM content c ");
        vecSb.append("JOIN users u ON c.user_id = u.id ");
        vecSb.append("CROSS JOIN (SELECT embedding FROM pets WHERE id = ?) AS p ");
        vecSb.append("WHERE c.embedding IS NOT NULL ");
        vecSb.append("AND p.embedding IS NOT NULL ");
        vecSb.append("AND c.content_type = ANY (?) ");
        vecSb.append("AND c.user_id <> ? ");
        if (businessOnly) {
            vecSb.append("AND u.role = 'business' ");
        }
        vecSb.append("ORDER BY distance ASC ");
        vecSb.append("LIMIT ?");

        final String vecSql = vecSb.toString();

        List<Content> results = jdbc.query(conn -> {
            PreparedStatement ps = conn.prepareStatement(vecSql);
            ps.setLong(1, petId);
            // create SQL text[] for content types on this connection
            java.sql.Array sqlArr = conn.createArrayOf("text", types);
            ps.setArray(2, sqlArr);
            ps.setLong(3, requestingUserId == null ? -1L : requestingUserId);
            ps.setInt(4, limit);
            return ps;
        }, new ContentRowMapper());

        if (results != null && !results.isEmpty()) {
            return results;
        }

        // Fallback to tag-match (if embedding search returned nothing)
        final String petTagsSql = "SELECT health_tags FROM pets WHERE id = ?";
        java.sql.Array petArr = null;
        try (Connection petConn = Objects.requireNonNull(jdbc.getDataSource()).getConnection();
             PreparedStatement psPet = petConn.prepareStatement(petTagsSql)) {

            psPet.setLong(1, petId);
            try (ResultSet rs = psPet.executeQuery()) {
                if (!rs.next()) return List.of();
                petArr = rs.getArray(1);
                if (petArr == null) return List.of();
                Object[] tagsObj = (Object[]) petArr.getArray();
                if (tagsObj == null || tagsObj.length == 0) return List.of();

                // Build final tag SQL (effectively final)
                StringBuilder tagSb = new StringBuilder();
                tagSb.append("SELECT c.*, u.id AS u_id, u.username, u.profile_picture, u.full_name, u.bio, u.role ");
                tagSb.append("FROM content c JOIN users u ON c.user_id = u.id ");
                tagSb.append("WHERE c.tags && ?::text[] ");
                tagSb.append("AND c.content_type = ANY (?) ");
                tagSb.append("AND c.user_id <> ? ");
                if (businessOnly) {
                    tagSb.append("AND u.role = 'business' ");
                }
                tagSb.append("ORDER BY c.created_at DESC ");
                tagSb.append("LIMIT ?");

                final String tagSql = tagSb.toString();

                List<Content> tagResults = jdbc.query(conn -> {
                    PreparedStatement p2 = conn.prepareStatement(tagSql);
                    java.sql.Array arr = conn.createArrayOf("text", tagsObj);
                    p2.setArray(1, arr);
                    java.sql.Array typeArr = conn.createArrayOf("text", types);
                    p2.setArray(2, typeArr);
                    p2.setLong(3, requestingUserId == null ? -1L : requestingUserId);
                    p2.setInt(4, limit);
                    return p2;
                }, new ContentRowMapper());

                if (tagResults != null && !tagResults.isEmpty()) return tagResults;
                return List.of();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching pet tags or tag-based recommendation", e);
        } finally {
            try { if (petArr != null) petArr.free(); } catch (Exception ignored) {}
        }
    }

}
