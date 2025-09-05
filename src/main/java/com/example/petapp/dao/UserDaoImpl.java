// UserDaoImpl.java
package com.example.petapp.dao;

import com.example.petapp.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbc;

    public UserDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private RowMapper<User> rowMapper = new RowMapper<>() {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setFullname(rs.getString("full_name"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setRole(rs.getString("role"));
            u.setAccountStatus(rs.getString("account_status"));
            u.setProfilePicture(rs.getBytes("profile_picture"));
            u.setBio(rs.getString("bio"));
            u.setBusinessType(rs.getString("business_type"));
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            u.setLastActive(rs.getTimestamp("last_active").toLocalDateTime());
            return u;
        }
    };

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return jdbc.query(sql, new Object[]{username}, rowMapper).stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbc.query(sql, new Object[]{email}, rowMapper).stream().findFirst();
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users(full_name,username,email,password_hash,role,account_status,profile_picture,bio,business_type) VALUES(?,?,?,?,?,?,?,?,?) RETURNING id,created_at,last_active";
        Object[] params = {
                user.getFullname(), user.getUsername(), user.getEmail(), user.getPasswordHash(),
                user.getRole(), user.getAccountStatus(), user.getProfilePicture(), user.getBio(),user.getBusinessType()
        };
        int[] types = {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.BINARY, Types.VARCHAR,Types.VARCHAR };
        return jdbc.queryForObject(sql, params, types, (rs, rowNum) -> {
            user.setId(rs.getLong("id"));
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setLastActive(rs.getTimestamp("last_active").toLocalDateTime());
            return user;
        });
    }

    @Override
    public void updateLastActive(long userId) {
        String sql = "UPDATE users SET last_active = NOW() WHERE id = ?";
        jdbc.update(sql, userId);
    }


    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.query(sql, new Object[]{id}, rowMapper).stream().findFirst();
    }


    @Override
    public void updateProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, bio = ?, account_status = ?, profile_picture = ?, business_type = ? WHERE id = ?";
        jdbc.update(sql,
                user.getFullname(),
                user.getEmail(),
                user.getBio(),
                user.getAccountStatus(),
                user.getProfilePicture(),
                user.getBusinessType(),
                user.getId()
        );
    }

    @Override
    public List<String> findDistinctBusinessTypes() {
        String sql = "SELECT DISTINCT business_type FROM users WHERE business_type IS NOT NULL ORDER BY business_type ASC";
        return jdbc.queryForList(sql, String.class);
    }

    @Override
    public boolean isBusinessUser(Long userId) {
        if (userId == null) return false;
        final String sql = "SELECT role FROM users WHERE id = ?";
        try {
            String role = jdbc.queryForObject(sql, new Object[]{userId}, String.class);
            return "business".equalsIgnoreCase(role);
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
    }

    @Override
    public List<User> findBusinesses(String businessType, Integer limit, Integer offset) {
        // only users with role = 'business' and not blocked
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, full_name, username, bio, profile_picture, business_type, account_status, role ");
        sql.append("FROM users WHERE role = 'business' ");

        List<Object> params = new ArrayList<>();
        if (businessType != null && !businessType.trim().isEmpty()) {
            sql.append("AND business_type = ? ");
            params.add(businessType);
        }

        // optional: exclude blocked accounts
        sql.append("AND account_status <> 'blocked' ");

        sql.append("ORDER BY full_name NULLS LAST, username ASC ");

        if (limit != null && limit > 0) {
            sql.append("LIMIT ? ");
            params.add(limit);
        }
        if (offset != null && offset >= 0) {
            sql.append("OFFSET ? ");
            params.add(offset);
        }

        return jdbc.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setFullname(rs.getString("full_name"));
            u.setUsername(rs.getString("username"));
            u.setBio(rs.getString("bio"));
            u.setProfilePicture(rs.getBytes("profile_picture"));
            u.setBusinessType(rs.getString("business_type"));
            u.setAccountStatus(rs.getString("account_status"));
            u.setRole(rs.getString("role"));
            // do NOT set createdAt/lastActive here for a lightweight listing, but ok to set if you prefer
            return u;
        });
    }


}