package com.example.petapp.dao;

import com.example.petapp.model.FollowStatus;
import com.example.petapp.model.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowDao {
    Optional<FollowStatus> findStatus(Long followerId, Long followingId);
    boolean create(Long followerId, Long followingId, FollowStatus status);
    boolean updateStatus(Long followerId, Long followingId, FollowStatus status);
    boolean delete(Long followerId, Long followingId);


    List<User> findIncomingRequestUsers(Long targetId);

    List<User> findAcceptedFollowerUsers(Long targetId);

    List<User> findFollowingUsers(Long followerId);
    List<User> findBlockedUsers(Long targetId);

    List<User> findIncomingRequestUsersSince(Long targetId, OffsetDateTime since);
    int countIncomingRequestsSince(Long targetId, OffsetDateTime since);

}
