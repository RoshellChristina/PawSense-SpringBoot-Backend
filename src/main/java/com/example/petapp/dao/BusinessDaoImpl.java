package com.example.petapp.dao;

import com.example.petapp.dto.BusinessAddressDTO;
import com.example.petapp.model.Address;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class BusinessDaoImpl implements BusinessDao {
    private final JdbcTemplate jdbc;

    private final RowMapper<Address> rm = (rs, rn) -> {
        Address a = new Address();
        a.setId(rs.getLong("id"));
        a.setUserId(rs.getLong("user_id"));
        a.setLongitude(rs.getDouble("lon"));
        a.setLatitude(rs.getDouble("lat"));
        a.setAddress(rs.getString("address"));
        a.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
        return a;
    };

    public BusinessDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    @Override
    public List<Address> findNearbyBusinesses(double lon, double lat, double radiusKm) {
        String sql = """
            SELECT
              ua.id,
              ua.user_id,
              ST_X(ua.location::geometry) AS lon,
              ST_Y(ua.location::geometry) AS lat,
              ua.address,
              ua.updated_at
            FROM user_addresses ua
            JOIN users u ON ua.user_id = u.id
            WHERE u.role = 'business'
              AND ST_DWithin(
                ua.location::geography,
                ST_SetSRID(ST_MakePoint(?, ?),4326)::geography,
                ?
              )
        """;

        RowMapper<Address> rm = (rs, rowNum) -> {
            Address a = new Address();
            a.setId(rs.getLong("id"));
            a.setUserId(rs.getLong("user_id"));
            a.setLongitude(rs.getDouble("lon"));
            a.setLatitude(rs.getDouble("lat"));
            a.setAddress(rs.getString("address"));
            a.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
            return a;
        };

        return jdbc.query(sql, rm, lon, lat, radiusKm * 1000);
    }

    // new method with DTO
    @Override
    public List<BusinessAddressDTO> findNearbyBusinessesWithType(double lon, double lat, double radiusKm) {
        String sql = """
            SELECT
              ua.id AS address_id,
              ua.user_id,
              ST_X(ua.location::geometry) AS longitude,
              ST_Y(ua.location::geometry) AS latitude,
              ua.address,
              u.fullname,
              u.business_type
            FROM user_addresses ua
            JOIN users u ON ua.user_id = u.id
            WHERE u.role = 'business'
              AND ST_DWithin(
                ua.location::geography,
                ST_SetSRID(ST_MakePoint(?, ?),4326)::geography,
                ?
              )
        """;

        return jdbc.query(sql, new BusinessAddressRowMapper(), lon, lat, radiusKm * 1000);
    }
}
