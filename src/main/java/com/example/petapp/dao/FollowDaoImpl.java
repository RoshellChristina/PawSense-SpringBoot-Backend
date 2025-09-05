package com.example.petapp.dao;

import com.example.petapp.model.FollowStatus;
import com.example.petapp.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Repository
public class FollowDaoImpl implements FollowDao {

    private final JdbcTemplate jdbc;

    @Autowired
    public FollowDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    @Override
    public Optional<FollowStatus> findStatus(Long followerId, Long followingId) {
        final String sql = "SELECT status FROM followers WHERE follower_id = ? AND following_id = ?";
        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(FollowStatus.valueOf(rs.getString("status")));
        }, followerId, followingId);
    }

    @Override
    public boolean create(Long followerId, Long followingId, FollowStatus status) {
        final String existsSql = "SELECT COUNT(*) FROM followers WHERE follower_id = ? AND following_id = ?";
        final String insertSql = "INSERT INTO followers (follower_id, following_id, status, created_at) VALUES (?, ?, ?, now())";

        try {
            Integer cnt = jdbc.queryForObject(existsSql, Integer.class, followerId, followingId);
            if (cnt != null && cnt > 0) {
                log.info("Follow already exists (no-op): follower={}, following={}", followerId, followingId);
                return false;
            }

            int updated = jdbc.update(insertSql, followerId, followingId, status.name());
            log.info("FollowDaoImpl.create -> rowsInserted = {} (follower={}, following={}, status={})",
                    updated, followerId, followingId, status.name());
            return updated == 1;
        } catch (DataAccessException dae) {
            log.error("SQL error inserting follow row: follower={}, following={}, status={}", followerId, followingId, status.name(), dae);
            throw dae;
        }
    }

    @Override
    public boolean updateStatus(Long followerId, Long followingId, FollowStatus status) {
        final String sql = "UPDATE followers SET status = ? WHERE follower_id = ? AND following_id = ?";
        try {
            int updated = jdbc.update(sql, status.name(), followerId, followingId);
            log.info("FollowDaoImpl.updateStatus -> rowsUpdated = {} (follower={}, following={}, status={})",
                    updated, followerId, followingId, status.name());
            return updated == 1;
        } catch (DataAccessException dae) {
            log.error("SQL error updating follow status", dae);
            throw dae;
        }
    }

    @Override
    public boolean delete(Long followerId, Long followingId) {
        final String sql = "DELETE FROM followers WHERE follower_id = ? AND following_id = ?";
        try {
            int deleted = jdbc.update(sql, followerId, followingId);
            log.info("FollowDaoImpl.delete -> rowsDeleted = {} (follower={}, following={})",
                    deleted, followerId, followingId);
            return deleted == 1;
        } catch (DataAccessException dae) {
            log.error("SQL error deleting follow row", dae);
            throw dae;
        }
    }

    @Override
    public List<User> findIncomingRequestUsers(Long targetId) {
        final String sql = """
        SELECT u.* 
        FROM followers f
        JOIN users u ON f.follower_id = u.id
        WHERE f.following_id = ? AND f.status = 'REQUESTED'
        ORDER BY f.created_at DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFullname(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setAccountStatus(rs.getString("account_status"));
            u.setBio(rs.getString("bio"));
            // add more fields if you need
            return u;
        }, targetId);
    }


    // FollowDaoImpl.java (add these methods)

    @Override
    public List<User> findAcceptedFollowerUsers(Long targetId) {
        final String sql = """
        SELECT u.*
        FROM followers f
        JOIN users u ON f.follower_id = u.id
        WHERE f.following_id = ? AND f.status = 'ACCEPTED'
        ORDER BY f.created_at DESC
        """;
        return jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFullname(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setAccountStatus(rs.getString("account_status"));
            u.setBio(rs.getString("bio"));
            u.setProfilePicture(rs.getBytes("profile_picture")); // important
            return u;
        }, targetId);
    }

    @Override
    public List<User> findFollowingUsers(Long followerId) {
        final String sql = """
        SELECT u.*
        FROM followers f
        JOIN users u ON f.following_id = u.id
        WHERE f.follower_id = ? AND f.status = 'ACCEPTED'
        ORDER BY f.created_at DESC
        """;
        return jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFullname(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setAccountStatus(rs.getString("account_status"));
            u.setBio(rs.getString("bio"));
            u.setProfilePicture(rs.getBytes("profile_picture"));
            return u;
        }, followerId);
    }

    @Override
    public List<User> findBlockedUsers(Long targetId) {
        final String sql = """
        SELECT u.*
        FROM followers f
        JOIN users u ON f.follower_id = u.id
        WHERE f.following_id = ? AND f.status = 'BLOCKED'
        ORDER BY f.created_at DESC
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFullname(rs.getString("full_name"));
            u.setEmail(rs.getString("email"));
            u.setAccountStatus(rs.getString("account_status"));
            u.setBio(rs.getString("bio"));
            u.setProfilePicture(rs.getBytes("profile_picture"));
            return u;
        }, targetId);
    }

    @Override
    public List<User> findIncomingRequestUsersSince(Long targetId, OffsetDateTime since) {
        final String sql = """
        SELECT u.*
        FROM followers f
        JOIN users u ON f.follower_id = u.id
        WHERE f.following_id = ? AND f.status = 'REQUESTED' AND f.created_at > ?
        ORDER BY f.created_at DESC
        """;
        return jdbc.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setFullname(rs.getString("full_name"));
            u.setProfilePicture(rs.getBytes("profile_picture"));
            u.setBio(rs.getString("bio"));
            // don't set timestamps unless you need them
            return u;
        }, targetId, Timestamp.from(since.toInstant()));
    }

    @Override
    public int countIncomingRequestsSince(Long targetId, OffsetDateTime since) {
        final String sql = "SELECT COUNT(*) FROM followers WHERE following_id = ? AND status = 'REQUESTED' AND created_at > ?";
        Integer cnt = jdbc.queryForObject(sql, Integer.class, targetId, Timestamp.from(since.toInstant()));
        return cnt == null ? 0 : cnt;
    }


}
