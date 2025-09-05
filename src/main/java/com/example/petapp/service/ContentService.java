package com.example.petapp.service;

import com.example.petapp.model.Content;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ContentService {
    Content create(Content content);
    Optional<Content> getById(Long id);
    List<Content> getAll();
    boolean update(Content content) throws IOException;
    boolean delete(Long id);
    List<Content> getByUserId(Long userId);
}
