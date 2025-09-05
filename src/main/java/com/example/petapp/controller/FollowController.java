package com.example.petapp.controller;

import com.example.petapp.dto.UserDTO;
import com.example.petapp.model.FollowStatus;
import com.example.petapp.service.FollowService;
import com.example.petapp.dao.UserDao;
import com.example.petapp.model.User;
import com.example.petapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserDao userDao;

    // check status: /api/follow/status?followerId=1&followingId=2
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status(@RequestParam Long followerId, @RequestParam Long followingId) {
        Optional<FollowStatus> s = followService.getStatus(followerId, followingId);
        return ResponseEntity.ok(Map.of("status", s.map(Enum::name).orElse("NONE")));
    }

    // request follow: body { "followerId": 1, "followingId": 2 }
    @PostMapping
    public ResponseEntity<Map<String, String>> follow(@RequestBody Map<String, Long> body) {
        Long followerId = body.get("followerId");
        Long followingId = body.get("followingId");
        if (followerId == null || followingId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "followerId and followingId required"));
        }

        Optional<User> target = userDao.findById(followingId);
        if (target.isEmpty()) return ResponseEntity.notFound().build();

        String accountStatus = target.get().getAccountStatus(); // "public" or "private"
        FollowStatus result = followService.requestFollow(followerId, followingId, accountStatus);
        return ResponseEntity.ok(Map.of("status", result.name()));
    }

    // unfollow
    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        boolean ok = followService.unfollow(followerId, followingId);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // list incoming requests for a user (target)
    @GetMapping("/requests")
    public ResponseEntity<List<UserDTO>> incoming(@RequestParam Long targetId) {
        List<User> users = followService.listIncomingRequestUsers(targetId);

        List<UserDTO> dtos = users.stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setFullname(u.getFullname());
            dto.setAccountStatus(u.getAccountStatus());
            dto.setBio(u.getBio());
            if (u.getProfilePicture() != null) {
                dto.setProfilePictureBase64(Base64.getEncoder().encodeToString(u.getProfilePicture()));
            }

            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/requests/{followerId}/accept")
    public ResponseEntity<Map<String,String>> accept(@PathVariable Long followerId, @RequestParam Long targetId) {
        boolean ok = followService.approveRequest(followerId, targetId);
        return ok ? ResponseEntity.ok(Map.of("status", "ACCEPTED")) : ResponseEntity.notFound().build();
    }

    @PostMapping("/requests/{followerId}/reject")
    public ResponseEntity<Map<String,String>> reject(@PathVariable Long followerId, @RequestParam Long targetId) {
        boolean ok = followService.rejectRequest(followerId, targetId);
        return ok ? ResponseEntity.ok(Map.of("status", "REJECTED")) : ResponseEntity.notFound().build();
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long userId) {
        List<UserDTO> dtos = followService.listAcceptedFollowerDTOs(userId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable Long userId) {
        List<UserDTO> dtos = followService.listFollowingDTOs(userId);
        return ResponseEntity.ok(dtos);
    }

    // in FollowController.java

    // Block a follower (target blocks follower)
    @PostMapping("/{followerId}/block")
    public ResponseEntity<Map<String,String>> blockFollower(@PathVariable Long followerId, @RequestParam Long targetId) {
        boolean ok = followService.blockFollower(followerId, targetId);
        return ok ? ResponseEntity.ok(Map.of("status", "BLOCKED")) : ResponseEntity.notFound().build();
    }

    // Unblock a follower (removes the row)
    @PostMapping("/{followerId}/unblock")
    public ResponseEntity<Map<String,String>> unblockFollower(@PathVariable Long followerId, @RequestParam Long targetId) {
        boolean ok = followService.unblockFollower(followerId, targetId);
        return ok ? ResponseEntity.ok(Map.of("status", "UNBLOCKED")) : ResponseEntity.notFound().build();
    }

    @GetMapping("/blocked/{userId}")
    public ResponseEntity<List<UserDTO>> getBlocked(@PathVariable Long userId) {
        List<UserDTO> dtos = followService.listBlockedDTOs(userId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/requests/new")
    public ResponseEntity<Map<String,Object>> newFollowRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);

            // fetch last_active from users table
            Optional<User> maybe = userDao.findById(userId);
            if (maybe.isEmpty()) return ResponseEntity.notFound().build();
            User me = maybe.get();
            OffsetDateTime lastActive = OffsetDateTime.from(me.getLastActive()); // might be null if never logged in

            // if null, treat as very old date so we return all requests
            OffsetDateTime since = lastActive != null ? lastActive : OffsetDateTime.parse("1970-01-01T00:00:00Z");

            List<User> reqUsers = followService.listIncomingRequestUsersSince(userId, since);
            int count = followService.countIncomingRequestsSince(userId, since);

            // map into DTOs (small UserDTO) so you don't leak password, etc.
            List<UserDTO> dtos = reqUsers.stream().map(u -> {
                UserDTO d = new UserDTO();
                d.setId(u.getId());
                d.setUsername(u.getUsername());
                d.setFullname(u.getFullname());
                d.setBio(u.getBio());
                if (u.getProfilePicture() != null) {
                    d.setProfilePictureBase64(Base64.getEncoder().encodeToString(u.getProfilePicture()));
                }
                return d;
            }).toList();

            return ResponseEntity.ok(Map.of("count", count, "requests", dtos));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }


}
