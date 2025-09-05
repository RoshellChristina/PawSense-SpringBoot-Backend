// src/main/java/com/example/petapp/dao/LostPetAlertDaoImpl.java
package com.example.petapp.dao;

import com.example.petapp.model.LostPetAlert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class LostPetAlertDaoImpl implements LostPetAlertDao {
    private final JdbcTemplate jdbc;
    @Autowired
    public LostPetAlertDaoImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public LostPetAlert create(LostPetAlert a) {
        final String sql = """
            INSERT INTO lost_pet_alerts
              (last_seen, last_seen_address, radius_km, created_at)
            VALUES (
              ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
              ?, ?, ?
            )
            RETURNING id
            """;

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            // Note: MakePoint expects (longitude, latitude)
            ps.setObject(1, a.getLongitude());
            ps.setObject(2, a.getLatitude());
            ps.setString(3, a.getLastSeenAddress());
            ps.setObject(4, a.getRadiusKm());
            ps.setObject(5, OffsetDateTime.now());
            return ps;
        }, kh);

        Long id = kh.getKey().longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public Optional<LostPetAlert> findById(Long id) {
        final String sql = """
            SELECT
              id,
              ST_X(last_seen::geometry) AS longitude,
              ST_Y(last_seen::geometry) AS latitude,
              last_seen_address,
              radius_km,
              created_at
            FROM lost_pet_alerts
            WHERE id = ?
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            LostPetAlert a = new LostPetAlert();
            a.setId(rs.getLong("id"));
            a.setLongitude(rs.getDouble("longitude"));
            a.setLatitude(rs.getDouble("latitude"));
            a.setLastSeenAddress(rs.getString("last_seen_address"));
            a.setRadiusKm(rs.getDouble("radius_km"));
            a.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            return a;
        }, id).stream().findFirst();
    }


    @Override
    public boolean update(LostPetAlert alert) {
        final String sql = """
            UPDATE lost_pet_alerts
               SET last_seen = ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                   last_seen_address = ?,
                   radius_km = ?
             WHERE id = ?
            """;
        return jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setObject(1, alert.getLongitude());
            ps.setObject(2, alert.getLatitude());
            ps.setString(3, alert.getLastSeenAddress());
            ps.setObject(4, alert.getRadiusKm());
            ps.setObject(5, alert.getId());
            return ps;
        }) == 1;
    }



    @Override
    public boolean deleteById(Long id) {
        return jdbc.update("DELETE FROM lost_pet_alerts WHERE id=?", id) == 1;
    }
}
