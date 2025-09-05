package com.example.petapp.controller;

import com.example.petapp.alerts.LostPetAlertManager;
import com.example.petapp.dao.UserAddressDao;
import com.example.petapp.dto.ContentRequest;
import com.example.petapp.dto.LostPetAlertDTO;
import com.example.petapp.model.Content;
import com.example.petapp.model.LostPetAlert;
import com.example.petapp.service.ContentService;
import com.example.petapp.service.LostPetAlertService;
import com.example.petapp.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.petapp.dto.LostAlertDto;     // matches your DTO filename/class
import com.example.petapp.dto.UserSummary;
import com.example.petapp.util.ThumbnailUtil;

import java.util.Map;
import java.util.stream.Collectors;
import com.example.petapp.model.LostPetAlert;
import com.example.petapp.model.User;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService service;

    @Autowired
    public ContentController(ContentService service) {
        this.service = service;
    }
    @Autowired private LostPetAlertService lostPetAlertService;
    @Autowired
    private UserAddressDao userAddressDao;
    @Autowired
    private ContentService contentService;

    @Autowired
    private LostPetAlertManager lostPetAlertManager;
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Content> create(@RequestBody ContentRequest req) throws IOException {

        Long alertId = null;
        if ("lost_pet_alert".equals(req.contentType)) {
            LostPetAlertDTO dto = new LostPetAlertDTO();
            dto.latitude = req.lastSeenLat;
            dto.longitude = req.lastSeenLng;
            dto.lastSeenAddress = req.lastSeenAddress;
            dto.radiusKm = req.radiusKm != null ? req.radiusKm : 2.0;
            LostPetAlertDTO saved = lostPetAlertService.create(dto);
            alertId = saved.id;
        }

        Content c = new Content();
        c.setUserId(req.userId);
        c.setContentType(req.contentType);
        c.setLostPetAlertId(alertId);
        c.setTitle(req.title);
        c.setBody(req.body);
        c.setTags(req.tags);
        c.setCreatedAt(OffsetDateTime.now());

        List<String> b64 = req.mediaBase64List;
        List<String> types = req.mediaTypeList;
        List<Integer> pos = req.positions;

        if (b64 != null) {
            for (int i = 0; i < b64.size() && i < 5; i++) {
                byte[] bytes = ImageUtil.decodeFromBase64(b64.get(i));
                int p = (pos != null && pos.size() > i) ? pos.get(i) : (i+1);
                String mt = types != null && types.size() > i ? types.get(i) : null;
                switch (p) {
                    case 1 -> { c.setMedia1(bytes); c.setMedia1Type(mt); }
                    case 2 -> { c.setMedia2(bytes); c.setMedia2Type(mt); }
                    case 3 -> { c.setMedia3(bytes); c.setMedia3Type(mt); }
                    case 4 -> { c.setMedia4(bytes); c.setMedia4Type(mt); }
                    case 5 -> { c.setMedia5(bytes); c.setMedia5Type(mt); }
                }
            }
        }

        Content savedContent = service.create(c);

        // If this was a lost_pet_alert, find nearby users and notify them
        if (alertId != null) {
            try {
                // fetch alert details (lat/lng/radius)
                Optional<LostPetAlert> maybeAlert = lostPetAlertService.getById(alertId);
                if (maybeAlert.isPresent()) {
                    LostPetAlert savedAlert = maybeAlert.get();
                    double lat = savedAlert.getLatitude() != null ? savedAlert.getLatitude() : 0.0;
                    double lng = savedAlert.getLongitude() != null ? savedAlert.getLongitude() : 0.0;
                    double radiusKm = savedAlert.getRadiusKm() != null ? savedAlert.getRadiusKm() : 2.0;

                    List<Long> nearbyUserIds = userAddressDao.findUserIdsWithinRadius(lat, lng, radiusKm);
                    if (nearbyUserIds == null) nearbyUserIds = Collections.emptyList();

                    // exclude the author (don't notify the poster)
                    nearbyUserIds.removeIf(uid -> uid.equals(savedContent.getUserId()));

                    if (!nearbyUserIds.isEmpty()) {
                        // notify observers (NotificationService should be registered as an observer)
                        lostPetAlertManager.notifyObservers(savedContent, savedAlert, nearbyUserIds);
                        log.info("Lost-pet alert {} created; notified {} users", savedContent.getId(), nearbyUserIds.size());
                    } else {
                        log.info("Lost-pet alert {} created; no nearby users found", savedContent.getId());
                    }
                } else {
                    log.warn("LostPetAlert with id {} not found after create()", alertId);
                }
            } catch (Exception ex) {
                // never fail the request because notification step had a problem
                log.error("Error while processing lost pet notifications for content {}: {}", savedContent.getId(), ex.getMessage(), ex);
            }
        }

        return ResponseEntity.ok(savedContent);

    }


    /** READ ONE */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Content> getOne(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** READ ALL */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Content>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    /** UPDATE (JSON with base64 fields) */
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestBody Content content
    ) throws IOException {
        content.setId(id);
        boolean ok = service.update(content);
        return ok
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** DELETE */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean ok = service.delete(id);
        return ok
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** GET all content for a specific user */
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Content>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @GetMapping(value = "/lost-alerts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<LostAlertDto>> getLostAlerts() {
        List<Content> all = service.getAll();

        List<LostAlertDto> out = all.stream()
                .filter(c -> "lost_pet_alert".equals(c.getContentType()))
                .map(c -> {
                    LostAlertDto d = new LostAlertDto();
                    d.id = c.getId();
                    d.title = c.getTitle();

                    // thumbnail: prefer generated thumbnail, fallback to original media1
                    if (c.getMedia1() != null && c.getMedia1().length > 0) {
                        try {
                            byte[] thumb = ThumbnailUtil.createThumbnail(c.getMedia1(), 120); // 120px max
                            d.thumbnailBase64 = ImageUtil.encodeToBase64(thumb);
                        } catch (Exception ex) {
                            d.thumbnailBase64 = ImageUtil.encodeToBase64(c.getMedia1());
                        }
                    } else {
                        d.thumbnailBase64 = null;
                    }

                    d.media1Type = c.getMedia1Type();
                    d.createdAt = c.getCreatedAt();

                    // Fetch LostPetAlert by id (your alerts are stored separately)
                    d.lastSeenLat = null;
                    d.lastSeenLng = null;
                    d.lastSeenAddress = null;
                    if (c.getLostPetAlertId() != null) {
                        Optional<LostPetAlert> maybeAlert = lostPetAlertService.getById(c.getLostPetAlertId());
                        if (maybeAlert.isPresent()) {
                            LostPetAlert la = maybeAlert.get();
                            d.lastSeenLat = la.getLatitude();
                            d.lastSeenLng = la.getLongitude();
                            d.lastSeenAddress = la.getLastSeenAddress();
                        }
                    }

                    // user summary — prefer populated User relation, otherwise fallback to userId only
                    try {
                        User u = c.getUser();
                        if (u != null) {
                            String profileB64 = (u.getProfilePicture() != null) ? ImageUtil.encodeToBase64(u.getProfilePicture()) : null;
                            // NOTE: your User model uses getFullname()
                            d.user = new UserSummary(u.getId(), u.getUsername(), u.getFullname(), profileB64);
                        } else {
                            d.user = new UserSummary(c.getUserId(), null, null, null);
                        }
                    } catch (Exception ex) {
                        d.user = new UserSummary(c.getUserId(), null, null, null);
                    }

                    return d;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(out);
    }

}
