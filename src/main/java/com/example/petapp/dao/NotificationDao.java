// src/main/java/com/example/petapp/dao/NotificationDao.java
package com.example.petapp.dao;

import com.example.petapp.model.Notification;
import java.util.List;

public interface NotificationDao {
    Notification create(Notification n);
    List<Notification> findByUserId(Long userId);
    boolean markAsRead(Long id, Long userId);
}
