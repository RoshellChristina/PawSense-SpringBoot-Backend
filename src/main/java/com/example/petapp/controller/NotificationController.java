// src/main/java/com/example/petapp/controller/NotificationController.java
package com.example.petapp.controller;

import com.example.petapp.model.Notification;
import com.example.petapp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;



@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService svc;

    public NotificationController(NotificationService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        return ResponseEntity.ok(svc.findByUserId(userId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        boolean ok = svc.markAsRead(id, userId);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        // default behavior: if your Principal.getName() returns numeric userId string
        try {
            return Long.valueOf(principal.getName());
        } catch (Exception e) {
            throw new IllegalStateException("Adapt getUserIdFromPrincipal to your security setup. Principal name: " + principal.getName());
        }
    }
}
