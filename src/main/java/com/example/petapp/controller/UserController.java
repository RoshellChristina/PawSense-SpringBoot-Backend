package com.example.petapp.controller;

import com.example.petapp.dao.UserDao;
import com.example.petapp.dto.UserDTO;
import com.example.petapp.dto.UserProfileUpdateRequest;
import com.example.petapp.model.Content;
import com.example.petapp.model.FollowStatus;
import com.example.petapp.model.User;
import com.example.petapp.service.ContentService;
import com.example.petapp.service.FollowService;
import com.example.petapp.service.UserService;
import com.example.petapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserDao userDao;
    private final JwtUtil jwtTokenUtil;
    private final UserService userService;// or however you extract userId from token
    private final ContentService contentService;
    private final FollowService followService;

    public UserController(UserDao userDao, JwtUtil jwtTokenUtil, UserService userService, ContentService contentService,
                          FollowService followService) {
        this.userDao = userDao;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.contentService = contentService;
        this.followService = followService;
    }

    @PostMapping("/last-active")
    public ResponseEntity<Void> updateLastActive(@RequestHeader("Authorization") String authHeader) {
        // extract token, then userId
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        userDao.updateLastActive(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody UserProfileUpdateRequest req) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        userService.updateUserProfile(userId, req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        Optional<User> u = userDao.findByUsername(username);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        Optional<User> u = userDao.findById(id);
        return u.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<Map<String,Object>> profile(@PathVariable String username, @RequestParam(required = false) Long viewerId) {
        Optional<User> maybe = userDao.findByUsername(username);
        if (maybe.isEmpty()) return ResponseEntity.notFound().build();
        User profileUser = maybe.get();

        boolean isSelf = viewerId != null && viewerId.equals(profileUser.getId());
        boolean canSee = false;

        if ("public".equalsIgnoreCase(profileUser.getAccountStatus())) {
            canSee = true;
        } else if (isSelf) {
            canSee = true;
        } else if (viewerId != null) {
            Optional<FollowStatus> status = followService.getStatus(viewerId, profileUser.getId());
            canSee = status.map(s -> s == FollowStatus.ACCEPTED).orElse(false);
        }

        List<Content> contents = Collections.emptyList();
        if (canSee) {
            contents = contentService.getByUserId(profileUser.getId()); // reuse existing service method
        }

        Map<String,Object> resp = Map.of(
                "user", profileUser,
                "canSeePosts", canSee,
                "contents", contents
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/business-types")
    public ResponseEntity<List<String>> getBusinessTypes() {
        return ResponseEntity.ok(userService.getBusinessTypes());
    }


}
