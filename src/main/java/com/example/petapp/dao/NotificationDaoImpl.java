// src/main/java/com/example/petapp/dao/NotificationDaoImpl.java
package com.example.petapp.dao;

import com.example.petapp.model.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class NotificationDaoImpl implements NotificationDao {
    private final JdbcTemplate jdbc;

    public NotificationDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Notification create(Notification n) {
        final String sql = "INSERT INTO notifications (user_id, content_id, type, title, message, link, is_read, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, n.getUserId());
            if (n.getContentId() != null) ps.setLong(2, n.getContentId()); else ps.setNull(2, Types.BIGINT);
            ps.setString(3, n.getType());
            ps.setString(4, n.getTitle());
            ps.setString(5, n.getMessage());
            ps.setString(6, n.getLink());
            ps.setBoolean(7, n.getIsRead() != null ? n.getIsRead() : false);
            ps.setTimestamp(8, Timestamp.from(java.time.Instant.now()));
            return ps;
        }, kh);

        Number key = kh.getKey();
        if (key != null) n.setId(key.longValue());
        n.setCreatedAt(OffsetDateTime.now());
        return n;
    }

    @Override
    public List<Notification> findByUserId(Long userId) {
        String sql = "SELECT id, user_id, content_id, type, title, message, link, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        return jdbc.query(sql, (rs, rowNum) -> {
            Notification n = new Notification();
            n.setId(rs.getLong("id"));
            n.setUserId(rs.getLong("user_id"));
            long cid = rs.getLong("content_id");
            if (!rs.wasNull()) n.setContentId(cid);
            n.setType(rs.getString("type"));
            n.setTitle(rs.getString("title"));
            n.setMessage(rs.getString("message"));
            n.setLink(rs.getString("link"));
            n.setIsRead(rs.getBoolean("is_read"));
            n.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            return n;
        }, userId);
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ? AND user_id = ?";
        int rows = jdbc.update(sql, id, userId);
        return rows > 0;
    }
}
