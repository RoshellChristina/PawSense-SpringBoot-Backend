package com.example.petapp.service;

import com.example.petapp.dao.ContentDao;
import com.example.petapp.model.Content;
import com.example.petapp.util.EmbeddingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ContentServiceImpl implements ContentService {

    private final ContentDao dao;
    private final EmbeddingClient embeddingClient;

    @Autowired
    public ContentServiceImpl(ContentDao dao, EmbeddingClient embeddingClient) {
        this.dao = dao;
        this.embeddingClient = embeddingClient;
    }


    @Override
    public Content create(Content content) {
        // decode any base64 into byte[]
        content.decodeBase64ToMedia();
        // compute embedding from title + body
        try {
            String textForEmbedding =
                    (content.getTitle() == null ? "" : content.getTitle() + " ") +
                            (content.getBody() == null ? "" : content.getBody() + " ") +
                            (content.getTags() == null ? "" : String.join(" ", content.getTags()));
            Double[] emb = embeddingClient.getEmbedding(textForEmbedding);
            content.setEmbedding(emb);
        } catch (Exception ex) {
            // do not fail content creation if embedding fails — log and continue
            System.err.println("Embedding failed during create: " + ex.getMessage());
            content.setEmbedding(null);
        }

        return dao.create(content);
    }

    @Override
    public Optional<Content> getById(Long id) {
        return dao.findById(id);
    }

    @Override
    public List<Content> getAll() {
        return dao.findAll();
    }

    @Override
    public boolean update(Content content) throws IOException {
        content.decodeBase64ToMedia();
        try {
            String textForEmbedding =
                    (content.getTitle() == null ? "" : content.getTitle() + " ") +
                            (content.getBody() == null ? "" : content.getBody() + " ") +
                            (content.getTags() == null ? "" : String.join(" ", content.getTags()));
            Double[] emb = embeddingClient.getEmbedding(textForEmbedding);
            content.setEmbedding(emb);
        } catch (Exception ex) {
            System.err.println("Embedding failed during update: " + ex.getMessage());
            // continue and perform update without embedding if embed server fails
        }

        return dao.update(content);
    }

    @Override
    public boolean delete(Long id) {
        return dao.deleteById(id);
    }

    @Override
    public List<Content> getByUserId(Long userId) {
        return dao.findByUserId(userId);
    }

}
