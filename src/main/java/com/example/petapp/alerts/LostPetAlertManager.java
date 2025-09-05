// src/main/java/com/example/petapp/alerts/LostPetAlertManager.java
package com.example.petapp.alerts;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import com.example.petapp.model.Content;
import com.example.petapp.model.LostPetAlert;

@Service
public class LostPetAlertManager {
    private final List<LostPetAlertObserver> observers = new ArrayList<>();

    public void register(LostPetAlertObserver o) { observers.add(o); }

    public void notifyObservers(Content content, LostPetAlert alert, List<Long> nearbyUserIds) {
        for (LostPetAlertObserver obs : observers) {
            try {
                obs.onLostPetAlert(content, alert, nearbyUserIds);
            } catch (Exception e) {
                // log and continue
                e.printStackTrace();
            }
        }
    }
}
