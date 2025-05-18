package com.geradev.mobilalk;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeterReadingJobService extends JobService {
    
    private static final String TAG = "MeterReadingJobService";
    private boolean jobCancelled = false;
    
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Mérőóra-leolvasási háttérfeladat indítása");
        
        // A háttérfeladat végrehajtása egy külön szálban
        doBackgroundWork(params);
        
        // Visszatérési érték true, ha a feladat aszinkron módon fut
        // és később mi hívjuk meg a jobFinished()-et
        return true;
    }
    
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Mérőóra-leolvasási háttérfeladat megszakítva");
        jobCancelled = true;
        
        // Visszatérési érték false, ha nem kell újraütemezni a feladatot
        return false;
    }
    
    private void doBackgroundWork(final JobParameters params) {
        new Thread(() -> {
            // Ellenőrizzük, hogy van-e bejelentkezett felhasználó
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.d(TAG, "Nincs bejelentkezett felhasználó");
                jobFinished(params, false);
                return;
            }
            
            // Lekérdezzük a felhasználó utolsó 3 leolvasását
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("readings")
                .whereEqualTo("userId", user.getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(3)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (jobCancelled) {
                        return;
                    }
                    
                    // A leolvasások feldolgozása
                    List<MeterReading> readings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        MeterReading reading = document.toObject(MeterReading.class);
                        reading.setId(document.getId());
                        readings.add(reading);
                        Log.d(TAG, "Leolvasás: " + reading.getReading() + " kWh, dátum: " + reading.getDate());
                    }
                    
                    // Ellenőrizzük, hogy van-e új leolvasás az utóbbi 30 napban
                    boolean needsReminder = checkIfReminderNeeded(readings);
                    
                    if (needsReminder) {
                        Log.d(TAG, "Emlékeztető küldése szükséges");
                        // Napi statisztika lekérése és mentése
                        calculateAndStoreDailyStats(user.getUid());
                        // Értesítés küldése
                        sendNotification();
                    } else {
                        Log.d(TAG, "Nincs szükség emlékeztetőre");
                    }
                    
                    // A feladat befejezése
                    jobFinished(params, false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Hiba a leolvasások lekérdezése közben", e);
                    jobFinished(params, true); // Újraütemezés hiba esetén
                });
        }).start();
    }
      private boolean checkIfReminderNeeded(List<MeterReading> readings) {
        if (readings.isEmpty()) {
            Log.d(TAG, "Nincs leolvasás, emlékeztető küldése");
            return true;
        }
        
        // Ellenőrizzük az utolsó leolvasás dátumát
        Date lastReadingDate = readings.get(0).getDate();        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();
        boolean needsReminder = lastReadingDate.before(thirtyDaysAgo);
        Log.d(TAG, "Utolsó leolvasás: " + lastReadingDate + ", szükséges emlékeztető: " + needsReminder);
        return needsReminder;
    }
      private void calculateAndStoreDailyStats(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -60);
        Date sixtyDaysAgo = calendar.getTime();
        
        db.collection("readings")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", sixtyDaysAgo)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.size() >= 2) {                    List<MeterReading> readings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        MeterReading reading = document.toObject(MeterReading.class);
                        readings.add(reading);
                    }
                    MeterReading firstReading = readings.get(0);
                    MeterReading lastReading = readings.get(readings.size() - 1);
                    
                    double totalConsumption = lastReading.getReading() - firstReading.getReading();
                    long daysDiff = (lastReading.getDate().getTime() - firstReading.getDate().getTime()) / (1000 * 60 * 60 * 24);
                    double avgDailyConsumption = (daysDiff > 0) ? totalConsumption / daysDiff : 0;
                      Map<String, Object> statData = new HashMap<>();
                    statData.put("userId", userId);
                    statData.put("calculationDate", new Date());
                    statData.put("avgDailyConsumption", avgDailyConsumption);
                    statData.put("totalConsumption", totalConsumption);
                    statData.put("periodDays", daysDiff);
                    
                    db.collection("statistics").add(statData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d(TAG, "Statisztika sikeresen mentve: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Hiba a statisztika mentésekor", e);
                        });
                }
            });
    }
      private void sendNotification() {
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        getApplicationContext().sendBroadcast(intent);
        Log.d(TAG, "Értesítési szándék elküldve a BroadcastReceiver-nek");
    }
}
