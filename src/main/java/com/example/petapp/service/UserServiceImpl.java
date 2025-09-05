// UserServiceImpl.java
package com.example.petapp.service;
import com.example.petapp.dao.AddressDao;
import com.example.petapp.dao.UserDao;
import com.example.petapp.dto.AddressDTO;
import com.example.petapp.dto.AddressUpdateRequest;
import com.example.petapp.dto.UserDTO;
import com.example.petapp.dto.UserProfileUpdateRequest;
import com.example.petapp.model.Address;
import com.example.petapp.model.User;
import com.example.petapp.util.ImageUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final AddressDao addressDao;

    public UserServiceImpl(UserDao userDao, AddressDao addressDao) {
        this.userDao = userDao;
        this.addressDao = addressDao;
    }

    @Override
    public UserDTO getUserProfile(Long userId) {
        User u = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDTO dto = mapUserToDto(u);
        List<AddressDTO> addrs = addressDao.findByUserId(userId)
                .stream()
                .map(a -> {
                    AddressDTO d = new AddressDTO();
                    d.setId(a.getId());
                    d.setAddress(a.getAddress());
                    d.setLatitude(a.getLatitude());
                    d.setLongitude(a.getLongitude());
                    return d;
                })
                .collect(Collectors.toList());
        dto.setAddresses(addrs);
        return dto;
    }

    @Override
    public void updateUserProfile(Long userId, UserProfileUpdateRequest req) {
        User u = userDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getFullname() != null) u.setFullname(req.getFullname());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getBio() != null) u.setBio(req.getBio());
        if (req.getAccountStatus() != null) u.setAccountStatus(req.getAccountStatus());
        if (req.getProfilePictureBase64() != null) {
            byte[] img = ImageUtil.decodeFromBase64(req.getProfilePictureBase64());
            u.setProfilePicture(img);
        }
        if (req.getBusinessType() != null) {
            u.setBusinessType(req.getBusinessType());
        }
        userDao.updateProfile(u);


        // addresses
        if (req.getAddresses() != null) {
            for (AddressUpdateRequest ar : req.getAddresses()) {
                Address m = new Address();
                m.setUserId(userId);
                m.setAddress(ar.getAddress());
                m.setLatitude(ar.getLatitude());
                m.setLongitude(ar.getLongitude());
                if (ar.getId() == null) {
                    addressDao.save(m);
                } else {
                    m.setId(ar.getId());
                    addressDao.update(m);
                }
            }
        }
    }
    private UserDTO mapUserToDto(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setRole(u.getRole());
        dto.setFullname(u.getFullname());
        dto.setEmail(u.getEmail());
        dto.setBio(u.getBio());
        dto.setAccountStatus(u.getAccountStatus());
        if (u.getProfilePicture() != null) {
            dto.setProfilePictureBase64(ImageUtil.encodeToBase64(u.getProfilePicture()));
        }
        dto.setBusinessType(u.getBusinessType());
        dto.setAddresses(new ArrayList<>());
        return dto;
    }

    @Override
    public List<String> getBusinessTypes() {
        return userDao.findDistinctBusinessTypes();
    }

    @Override
    public List<UserDTO> listBusinesses(String businessType, Integer limit, Integer offset) {
        // default paging
        int lim = (limit == null || limit <= 0) ? 20 : limit;
        int off = (offset == null || offset < 0) ? 0 : offset;

        List<com.example.petapp.model.User> users = userDao.findBusinesses(businessType, lim, off);

        return users.stream().map(u -> {
            UserDTO dto = new UserDTO();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setFullname(u.getFullname());
            dto.setBio(u.getBio());
            dto.setBusinessType(u.getBusinessType());
            if (u.getProfilePicture() != null) {
                dto.setProfilePictureBase64(ImageUtil.encodeToBase64(u.getProfilePicture()));
            }
            // keep addresses empty - this is a listing view
            dto.setAddresses(new ArrayList<>());
            dto.setRole(u.getRole());
            dto.setAccountStatus(u.getAccountStatus());
            return dto;
        }).collect(Collectors.toList());
    }


}

