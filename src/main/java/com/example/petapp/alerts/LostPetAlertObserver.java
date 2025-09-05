// src/main/java/com/example/petapp/alerts/LostPetAlertObserver.java
package com.example.petapp.alerts;

import com.example.petapp.model.Content;
import com.example.petapp.model.LostPetAlert;
import java.util.List;

public interface LostPetAlertObserver {
    void onLostPetAlert(Content content, LostPetAlert alert, List<Long> nearbyUserIds);
}
