package com.example.petapp.service;

import com.example.petapp.dto.UserDTO;
import com.example.petapp.model.FollowStatus;
import com.example.petapp.model.User;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FollowService {
    Optional<FollowStatus> getStatus(Long followerId, Long followingId);
    FollowStatus requestFollow(Long followerId, Long followingId, String targetAccountStatus); // returns resulting status
    boolean unfollow(Long followerId, Long followingId);

    boolean approveRequest(Long followerId, Long followingId); // followerId = requester, followingId = target/owner
    boolean rejectRequest(Long followerId, Long followingId);
    List<User> listIncomingRequestUsers(Long targetId);

    List<UserDTO> listAcceptedFollowerDTOs(Long targetId);

    List<UserDTO> listBlockedDTOs(Long targetId);

    List<UserDTO> listFollowingDTOs(Long followerId);
    // in FollowService.java
    boolean blockFollower(Long followerId, Long targetId);
    boolean unblockFollower(Long followerId, Long targetId);

    List<User> listIncomingRequestUsersSince(Long targetId, OffsetDateTime since);
    int countIncomingRequestsSince(Long targetId, OffsetDateTime since);


}
