// src/main/java/com/example/petapp/dao/AddressDaoImpl.java
package com.example.petapp.dao;

import com.example.petapp.model.Address;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class AddressDaoImpl implements AddressDao {
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

    public AddressDaoImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Address> findByUserId(Long userId) {
        String sql = """
            SELECT
              id,
              user_id,
              ST_X(location::geometry) AS lon,
              ST_Y(location::geometry) AS lat,
              address,
              updated_at
            FROM user_addresses
            WHERE user_id = ?
        """;
        return jdbc.query(sql, rm, userId);
    }

    @Override
    public void save(Address a) {
        String sql = """
            INSERT INTO user_addresses(user_id, location, address)
            VALUES(?, ST_SetSRID(ST_MakePoint(?, ?),4326), ?)
        """;
        jdbc.update(
                sql,
                new Object[]{
                        a.getUserId(),
                        a.getLongitude(),  // lon
                        a.getLatitude(),   // lat
                        a.getAddress()
                },
                new int[]{
                        Types.BIGINT,
                        Types.DOUBLE, Types.DOUBLE,
                        Types.VARCHAR
                }
        );
    }

    @Override
    public void update(Address a) {
        String sql = """
            UPDATE user_addresses
               SET location   = ST_SetSRID(ST_MakePoint(?, ?),4326),
                   address    = ?,
                   updated_at = now()
             WHERE id = ?
        """;
        jdbc.update(
                sql,
                a.getLongitude(),
                a.getLatitude(),
                a.getAddress(),
                a.getId()
        );
    }

    @Override
    public void delete(Long addressId) {
        jdbc.update("DELETE FROM user_addresses WHERE id = ?", addressId);
    }

    @Override
    public List<Address> findNearbyBusinesses(Long userId) {
        String sql = """
        SELECT ua.id,
               ua.user_id,
               ST_X(ua.location::geometry) AS lon,
               ST_Y(ua.location::geometry) AS lat,
               ua.address,
               ua.updated_at
          FROM user_addresses ua
          JOIN users u ON ua.user_id = u.id
         WHERE u.role = 'business'
           AND ua.location IS NOT NULL
         ORDER BY ua.location <-> (SELECT location FROM user_addresses WHERE user_id = ?)
         LIMIT 20
    """;
        return jdbc.query(sql, rm, userId);
    }


}
