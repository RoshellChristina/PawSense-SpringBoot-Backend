// src/main/java/com/example/petapp/mapper/BusinessAddressRowMapper.java
package com.example.petapp.dao;

import com.example.petapp.dto.BusinessAddressDTO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BusinessAddressRowMapper implements RowMapper<BusinessAddressDTO> {
    @Override
    public BusinessAddressDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        BusinessAddressDTO dto = new BusinessAddressDTO();
        dto.setId(rs.getLong("address_id"));
        dto.setUserId(rs.getLong("user_id"));
        dto.setAddress(rs.getString("address"));
        dto.setLatitude(rs.getDouble("latitude"));
        dto.setLongitude(rs.getDouble("longitude"));
        dto.setFullname(rs.getString("fullname"));
        dto.setBusinessType(rs.getString("business_type"));
        return dto;
    }
}
