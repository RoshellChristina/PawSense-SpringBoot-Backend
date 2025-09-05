package com.example.petapp.service;

import com.example.petapp.dao.FollowDao;
import com.example.petapp.dto.UserDTO;
import com.example.petapp.model.FollowStatus;
import com.example.petapp.model.User;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    private final FollowDao followDao;

    @Autowired
    public FollowServiceImpl(FollowDao followDao) {
        this.followDao = followDao;
    }

    @Override
    public Optional<FollowStatus> getStatus(Long followerId, Long followingId) {
        return followDao.findStatus(followerId, followingId);
    }

    @Override
    public FollowStatus requestFollow(Long followerId, Long followingId, String targetAccountStatus) {
        // if public -> ACCEPTED auto
        FollowStatus toSet = "public".equalsIgnoreCase(targetAccountStatus) ? FollowStatus.ACCEPTED : FollowStatus.REQUESTED;
        // try create; if exists update if needed
        boolean created = followDao.create(followerId, followingId, toSet);
        if (!created) {
            // already exists: update status if different
            Optional<FollowStatus> maybe = followDao.findStatus(followerId, followingId);
            if (maybe.isPresent()) {
                FollowStatus current = maybe.get();
                if (current != toSet) followDao.updateStatus(followerId, followingId, toSet);
                return toSet;
            } else {
                // fallback: create again
                followDao.create(followerId, followingId, toSet);
            }
        }
        return toSet;
    }

    @Override
    public boolean unfollow(Long followerId, Long followingId) {
        return followDao.delete(followerId, followingId);
    }

    @Override
    public boolean approveRequest(Long followerId, Long followingId) {
        // only change REQUESTED -> ACCEPTED
        return followDao.updateStatus(followerId, followingId, FollowStatus.ACCEPTED);
    }

    @Override
    public boolean rejectRequest(Long followerId, Long followingId) {
        return followDao.updateStatus(followerId, followingId, FollowStatus.REJECTED);
    }

    @Override
    public List<User> listIncomingRequestUsers(Long targetId) {
        return followDao.findIncomingRequestUsers(targetId);
    }

    // FollowServiceImpl.java (add these methods)

    // helper to convert User -> UserDTO (encode picture)
    private UserDTO toDto(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setFullname(u.getFullname());
        dto.setBio(u.getBio());
        dto.setAccountStatus(u.getAccountStatus());
        if (u.getProfilePicture() != null) {
            dto.setProfilePictureBase64(Base64.getEncoder().encodeToString(u.getProfilePicture()));
        }
        return dto;
    }

    @Override
    public List<UserDTO> listAcceptedFollowerDTOs(Long targetId) {
        List<User> users = followDao.findAcceptedFollowerUsers(targetId);
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> listBlockedDTOs(Long targetId) {
        List<User> users = followDao.findBlockedUsers(targetId);
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> listFollowingDTOs(Long followerId) {
        List<User> users = followDao.findFollowingUsers(followerId);
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    // in FollowServiceImpl.java

    @Override
    public boolean blockFollower(Long followerId, Long targetId) {
        // set status BLOCKED for the pair follower->target
        return followDao.updateStatus(followerId, targetId, FollowStatus.BLOCKED);
    }

    @Override
    public boolean unblockFollower(Long followerId, Long targetId) {
        // Try to update existing row from BLOCKED -> ACCEPTED (or any status -> ACCEPTED)
        boolean updated = followDao.updateStatus(followerId, targetId, FollowStatus.ACCEPTED);
        if (updated) {
            return true;
        }
        // if no row existed, insert one as ACCEPTED (so unblock ensures they're following)
        return followDao.create(followerId, targetId, FollowStatus.ACCEPTED);
    }

    @Override
    public List<User> listIncomingRequestUsersSince(Long targetId, OffsetDateTime since) {
        return followDao.findIncomingRequestUsersSince(targetId, since);
    }

    @Override
    public int countIncomingRequestsSince(Long targetId, OffsetDateTime since) {
        return followDao.countIncomingRequestsSince(targetId, since);
    }




}
