package com.example.petapp.dao;

import com.example.petapp.model.Content;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class
ContentRowMapper implements RowMapper<Content> {
    @Override
    public Content mapRow(ResultSet rs, int rowNum) throws SQLException {
        Content c = new Content();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setContentType(rs.getString("content_type"));
        c.setTitle(rs.getString("title"));
        c.setBody(rs.getString("body"));
        String tagsCsv = rs.getString("tags");
        if (tagsCsv != null && !tagsCsv.isBlank()) {
            List<String> tags = Arrays.stream(tagsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            c.setTags(tags);
        } else {
            c.setTags(Collections.emptyList());
        }

        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toInstant().atOffset(OffsetDateTime.now().getOffset()));
        return c;
    }
}
