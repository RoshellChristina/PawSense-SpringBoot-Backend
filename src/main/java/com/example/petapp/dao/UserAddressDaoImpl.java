// src/main/java/com/example/petapp/dao/UserAddressDaoImpl.java
package com.example.petapp.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserAddressDaoImpl implements UserAddressDao {

    private final JdbcTemplate jdbc;

    public UserAddressDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Long> findUserIdsWithinRadius(double lat, double lng, double radiusKm) {
        String sql = """
            SELECT ua.user_id
            FROM user_addresses ua
            JOIN (
                SELECT user_id, max(updated_at) as max_updated
                FROM user_addresses
                GROUP BY user_id
            ) latest ON latest.user_id = ua.user_id AND latest.max_updated = ua.updated_at
            WHERE ua.latitude IS NOT NULL AND ua.longitude IS NOT NULL
            AND (6371 * acos(
                   cos(radians(?)) *
                   cos(radians(ua.latitude)) *
                   cos(radians(ua.longitude) - radians(?)) +
                   sin(radians(?)) * sin(radians(ua.latitude))
                )) <= ?
            """;
        return jdbc.queryForList(sql, Long.class, lat, lng, lat, radiusKm);
    }
}
