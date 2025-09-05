// UserDao.java (Interface)
package com.example.petapp.dao;

import com.example.petapp.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user);
    void updateLastActive(long userId);
    Optional<User> findById(Long id);
    void updateProfile(User user);
    List<String> findDistinctBusinessTypes();
    boolean isBusinessUser(Long userId);
    List<User> findBusinesses(String businessType, Integer limit, Integer offset);
}