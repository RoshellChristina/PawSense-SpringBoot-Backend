package com.example.petapp.controller;

import com.example.petapp.dao.ContentInteractionDao;
import com.example.petapp.model.ContentStats;
import com.example.petapp.dto.CommentDto;
import com.example.petapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/contents")
public class ContentInteractionController {

    @Autowired
    private ContentInteractionDao dao;

    @Autowired
    private JwtUtil jwtUtil;

    private Long userIdFromReq(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if (bearer == null) return null;
        String token = bearer.startsWith("Bearer ") ? bearer.substring(7) : bearer;
        token = token.trim().replaceAll("^\"|\"$", ""); // remove quotes if any
        return jwtUtil.getUserIdFromToken(token);
    }

    // GET comments (all)
    @GetMapping("/{contentId}/comments")
    public List<CommentDto> getComments(@PathVariable long contentId) {
        return dao.getAllComments(contentId);
    }

    @PostMapping("/{contentId}/react")
    public ResponseEntity<?> react(@PathVariable long contentId, @RequestBody ReactRequest body, HttpServletRequest req) {
        Long userId = userIdFromReq(req);
        if (userId == null) return ResponseEntity.status(401).body("Unauthorized");
        String type = body.getType();
        if ("remove".equals(type)) {
            dao.removeReaction(contentId, userId);
        } else if ("like".equals(type) || "dislike".equals(type)) {
            dao.upsertReaction(contentId, userId, type);
        } else {
            return ResponseEntity.badRequest().body("Invalid reaction type");
        }
        ContentStats stats = dao.getStatsForContent(contentId, userId);
        return ResponseEntity.ok(stats);
    }

    public static class ReactRequest {
        private String type; // like|dislike|remove
        public String getType(){return type;}
        public void setType(String t){type=t;}
    }

    @PostMapping("/{contentId}/save-toggle")
    public ResponseEntity<?> toggleSave(@PathVariable long contentId, HttpServletRequest req) {
        Long userId = userIdFromReq(req);
        if (userId == null) return ResponseEntity.status(401).body("Unauthorized");
        boolean wasSaved = dao.isSavedByUser(contentId, userId);
        if (wasSaved) dao.unsaveContent(contentId, userId);
        else dao.saveContent(contentId, userId);
        ContentStats stats = dao.getStatsForContent(contentId, userId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{contentId}/view")
    public ResponseEntity<?> addView(@PathVariable long contentId, HttpServletRequest req) {
        Long userId = userIdFromReq(req); // can be null
        dao.addView(contentId, userId);
        ContentStats stats = dao.getStatsForContent(contentId, userId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{contentId}/comments")
    public ResponseEntity<?> addComment(@PathVariable long contentId, @RequestBody CommentRequest r, HttpServletRequest req) {
        Long userId = userIdFromReq(req);
        if (userId == null) return ResponseEntity.status(401).body("Unauthorized");
        // you might want to fetch username from users table; for simplicity we'll set username "You"
        CommentDto dto = dao.addComment(contentId, userId, r.getUsername()==null ? "You" : r.getUsername(), r.getBody());
        ContentStats stats = dao.getStatsForContent(contentId, userId);
        return ResponseEntity.ok(new AddCommentResponse(dto, stats));
    }

    public static class CommentRequest { private String body; private String username; public String getBody(){return body;} public void setBody(String b){body=b;} public String getUsername(){return username;} public void setUsername(String u){username=u;} }
    public static class AddCommentResponse {
        public CommentDto comment;
        public ContentStats stats;
        public AddCommentResponse(CommentDto c, ContentStats s){ comment = c; stats = s; }
    }

    @GetMapping("/{contentId}/stats")
    public ResponseEntity<?> getStats(@PathVariable long contentId, HttpServletRequest req) {
        Long userId = userIdFromReq(req);
        ContentStats stats = dao.getStatsForContent(contentId, userId);
        return ResponseEntity.ok(stats);
    }
}
