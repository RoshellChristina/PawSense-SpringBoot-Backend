// src/main/java/com/example/petapp/service/NotificationService.java
package com.example.petapp.service;

import com.example.petapp.alerts.LostPetAlertObserver;
import com.example.petapp.alerts.LostPetAlertManager;
import com.example.petapp.dao.NotificationDao;
import com.example.petapp.model.Content;
import com.example.petapp.model.LostPetAlert;
import com.example.petapp.model.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationService implements LostPetAlertObserver {

    private final NotificationDao notificationDao;
    private final SimpMessagingTemplate messagingTemplate;
    private final LostPetAlertManager manager;

    public NotificationService(NotificationDao notificationDao,
                               SimpMessagingTemplate messagingTemplate,
                               LostPetAlertManager manager) {
        this.notificationDao = notificationDao;
        this.messagingTemplate = messagingTemplate;
        this.manager = manager;
    }

    @PostConstruct
    public void init() {
        manager.register(this);
    }

    @Override
    public void onLostPetAlert(Content content, LostPetAlert alert, List<Long> nearbyUserIds) {
        for (Long recipientId : nearbyUserIds) {
            if (recipientId.equals(content.getUserId())) continue; // don't notify author

            Notification n = new Notification();
            n.setUserId(recipientId);
            n.setContentId(content.getId());
            n.setType("lost_pet_alert");
            n.setTitle("Lost pet near you: " + (content.getTitle() != null ? content.getTitle() : "A pet"));
            n.setMessage(content.getBody());
            n.setLink("/contents/" + content.getId());
            n.setIsRead(false);
            n.setCreatedAt(OffsetDateTime.now());

            Notification saved = notificationDao.create(n);

            // Push via STOMP to /user/{userId}/queue/notifications
            try {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipientId),   // principal name must match this string
                        "/queue/notifications",
                        saved
                );
            } catch (Exception e) {
                // if push fails, we still have saved notification in DB
                e.printStackTrace();
            }
        }
    }

    // helper methods for controller use
    public List<Notification> findByUserId(Long userId) {
        return notificationDao.findByUserId(userId);
    }

    public boolean markAsRead(Long id, Long userId) {
        return notificationDao.markAsRead(id, userId);
    }
}
