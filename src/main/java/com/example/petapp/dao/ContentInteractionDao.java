package com.example.petapp.dao;

import com.example.petapp.model.ContentStats;
import com.example.petapp.dto.CommentDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class ContentInteractionDao {

    @Autowired
    private JdbcTemplate jdbc;

    // ---- COMMENTS ----
    public CommentDto addComment(long contentId, long userId, String body) {
        // Insert and return id + created_at; also join username after insert
        String insertSql = "INSERT INTO content_comments (content_id, user_id, body) VALUES (?, ?, ?) RETURNING id, created_at";
        return jdbc.queryForObject(insertSql, new Object[]{contentId, userId, body}, (rs, rn) -> {
            long id = rs.getLong("id");
            OffsetDateTime created = rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC);
            // fetch username
            String username = jdbc.queryForObject("SELECT username FROM users WHERE id = ?", new Object[]{userId}, String.class);
            CommentDto dto = new CommentDto();
            dto.setId(id);
            dto.setContentId(contentId);
            dto.setUserId(userId);
            dto.setBody(body);
            dto.setUsername(username);
            dto.setCreatedAt(created);
            return dto;
        });
    }

    public List<CommentDto> getComments(long contentId, int limit, int offset) {
        // return paginated comments (oldest first)
        String sql = "SELECT c.id, c.content_id, c.user_id, c.body, c.created_at, u.username " +
                "FROM content_comments c LEFT JOIN users u ON u.id = c.user_id " +
                "WHERE c.content_id = ? ORDER BY c.created_at ASC LIMIT ? OFFSET ?";
        return jdbc.query(sql, new Object[]{contentId, limit, offset}, (ResultSet rs, int rowNum) -> {
            CommentDto d = new CommentDto();
            d.setId(rs.getLong("id"));
            d.setContentId(rs.getLong("content_id"));
            d.setUserId(rs.getLong("user_id"));
            d.setBody(rs.getString("body"));
            d.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC));
            d.setUsername(rs.getString("username"));
            return d;
        });
    }

    public List<CommentDto> getAllComments(long contentId) {
        String sql = "SELECT c.id, c.content_id, c.user_id, c.body, c.created_at, u.username " +
                "FROM content_comments c LEFT JOIN users u ON u.id = c.user_id " +
                "WHERE c.content_id = ? ORDER BY c.created_at ASC";
        return jdbc.query(sql, new Object[]{contentId}, (rs, rn) -> {
            CommentDto d = new CommentDto();
            d.setId(rs.getLong("id"));
            d.setContentId(rs.getLong("content_id"));
            d.setUserId(rs.getLong("user_id"));
            d.setBody(rs.getString("body"));
            d.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC));
            d.setUsername(rs.getString("username"));
            return d;
        });
    }


    // upsert reaction
    public void upsertReaction(long contentId, long userId, String reactionType) {
        String sql = "INSERT INTO content_reactions (content_id, user_id, reaction_type) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (content_id, user_id) DO UPDATE SET reaction_type = EXCLUDED.reaction_type, created_at = now()";
        jdbc.update(sql, contentId, userId, reactionType);
    }

    public void removeReaction(long contentId, long userId) {
        String sql = "DELETE FROM content_reactions WHERE content_id = ? AND user_id = ?";
        jdbc.update(sql, contentId, userId);
    }

    public void addView(long contentId, Long userId) {
        if (userId == null) {
            jdbc.update("INSERT INTO content_views (content_id) VALUES (?)", contentId);
        } else {
            jdbc.update("INSERT INTO content_views (content_id, user_id) VALUES (?, ?)", contentId, userId);
        }
    }



    public void toggleSave(long contentId, long userId) {
        // toggle: insert if not exists, otherwise delete
        String insert = "INSERT INTO content_saves (content_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        jdbc.update(insert, contentId, userId);
        // if was inserted, return; otherwise delete (we'll check by deleting and checking rows affected)
        String delete = "DELETE FROM content_saves WHERE content_id = ? AND user_id = ? AND id NOT IN (SELECT id FROM content_saves WHERE content_id = ? AND user_id = ? LIMIT 1)";
        // simpler: check if exists
        String existsSql = "SELECT 1 FROM content_saves WHERE content_id = ? AND user_id = ?";
        List<Integer> rows = jdbc.query(existsSql, new Object[]{contentId, userId}, (rs, rowNum) -> 1);
        if (rows.isEmpty()) {
            // inserted above, nothing to delete
            return;
        } else {
            // If already existed before insert, delete (toggle)
            // We'll delete the row (effectively toggling)
            String deleteExisting = "DELETE FROM content_saves WHERE content_id = ? AND user_id = ?";
            // But careful: since insert...on conflict did nothing if existed, we delete on toggle:
            // Determine prior existence by trying to insert and checking update count would've been 1 - JdbcTemplate doesn't return that.
            // Simpler approach: if exists -> delete; else -> insert (so do explicit check)
            // We'll rewrite toggle with check:
            String check = "SELECT COUNT(*) FROM content_saves WHERE content_id = ? AND user_id = ?";
            Integer count = jdbc.queryForObject(check, Integer.class, contentId, userId);
            if (count != null && count > 0) {
                jdbc.update("DELETE FROM content_saves WHERE content_id = ? AND user_id = ?", contentId, userId);
            } else {
                jdbc.update("INSERT INTO content_saves (content_id, user_id) VALUES (?, ?)", contentId, userId);
            }
        }
    }

    // simpler explicit methods:
    public boolean isSavedByUser(long contentId, long userId) {
        Integer cnt = jdbc.queryForObject("SELECT COUNT(*) FROM content_saves WHERE content_id = ? AND user_id = ?", Integer.class, contentId, userId);
        return cnt != null && cnt > 0;
    }

    public void saveContent(long contentId, long userId) {
        jdbc.update("INSERT INTO content_saves (content_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING", contentId, userId);
    }

    public void unsaveContent(long contentId, long userId) {
        jdbc.update("DELETE FROM content_saves WHERE content_id = ? AND user_id = ?", contentId, userId);
    }

    public long countLikes(long contentId) {
        Long l = jdbc.queryForObject("SELECT COUNT(*) FROM content_reactions WHERE content_id = ? AND reaction_type = 'like'", Long.class, contentId);
        return l == null ? 0L : l;
    }
    public long countDislikes(long contentId) {
        Long l = jdbc.queryForObject("SELECT COUNT(*) FROM content_reactions WHERE content_id = ? AND reaction_type = 'dislike'", Long.class, contentId);
        return l == null ? 0L : l;
    }
    public long countComments(long contentId) {
        Long l = jdbc.queryForObject("SELECT COUNT(*) FROM content_comments WHERE content_id = ?", Long.class, contentId);
        return l == null ? 0L : l;
    }
    public long countSaves(long contentId) {
        Long l = jdbc.queryForObject("SELECT COUNT(*) FROM content_saves WHERE content_id = ?", Long.class, contentId);
        return l == null ? 0L : l;
    }
    public long countViews(long contentId) {
        Long l = jdbc.queryForObject(
                "SELECT COUNT(*) FROM content_views WHERE content_id = ?",
                Long.class,
                contentId
        );
        return l == null ? 0L : l;
    }


    public String getUserReaction(long contentId, long userId) {
        List<String> list = jdbc.query("SELECT reaction_type FROM content_reactions WHERE content_id = ? AND user_id = ?", new Object[]{contentId, userId},
                (rs, rn) -> rs.getString("reaction_type"));
        return list.isEmpty() ? null : list.get(0);
    }

    // Comments handling
    public CommentDto addComment(long contentId, long userId, String username, String body) {
        String insert = "INSERT INTO content_comments (content_id, user_id, body) VALUES (?, ?, ?) RETURNING id, created_at";
        return jdbc.queryForObject(insert, new Object[]{contentId, userId, body}, (rs, rn) -> {
            CommentDto dto = new CommentDto();
            dto.setId(rs.getLong("id"));
            dto.setContentId(contentId);
            dto.setUserId(userId);
            dto.setBody(body);
            dto.setUsername(username);
            dto.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC));
            return dto;
        });
    }

    public List<CommentDto> getComments(long contentId) {
        String sql = "SELECT c.id, c.content_id, c.user_id, c.body, c.created_at, u.username FROM content_comments c LEFT JOIN users u ON u.id = c.user_id WHERE c.content_id = ? ORDER BY c.created_at ASC";
        return jdbc.query(sql, new Object[]{contentId}, (rs, rn) -> {
            CommentDto d = new CommentDto();
            d.setId(rs.getLong("id"));
            d.setContentId(rs.getLong("content_id"));
            d.setUserId(rs.getLong("user_id"));
            d.setBody(rs.getString("body"));
            d.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC));
            try { d.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
            return d;
        });
    }

    public ContentStats getStatsForContent(long contentId, Long currentUserId) {
        ContentStats s = new ContentStats();
        s.setLikes(countLikes(contentId));
        s.setDislikes(countDislikes(contentId));
        s.setComments(countComments(contentId));
        s.setSaves(countSaves(contentId));
        s.setViews(countViews(contentId));
        if (currentUserId != null) {
            s.setMyReaction(getUserReaction(contentId, currentUserId));
            s.setSavedByMe(isSavedByUser(contentId, currentUserId));
        }
        return s;
    }
}
