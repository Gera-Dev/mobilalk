package com.geradev.mobilalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "meter_reading_channel";
    private static final String TAG = "NotificationReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Értesítési esemény fogadása");
        createNotificationChannel(context);
        showNotification(context);
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: Android O vagy újabb verzió, csatorna létrehozása");
            CharSequence name = "Mérőóra Értesítések";
            String description = "Mérőóra-leolvasási emlékeztetők";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "createNotificationChannel: Értesítési csatorna sikeresen létrehozva");
            } else {
                Log.e(TAG, "createNotificationChannel: Nem sikerült a NotificationManager elérése");
            }
        } else {
            Log.d(TAG, "createNotificationChannel: Android N vagy régebbi verzió, nem szükséges csatorna");
        }
    }
    
    private void showNotification(Context context) {
        Log.d(TAG, "showNotification: Értesítés megjelenítése");
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Mérőóra leolvasás")
            .setContentText("Ideje leolvasni a mérőórádat!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "showNotification: Értesítés sikeresen elküldve");
        } catch (SecurityException e) {
            String errorMsg = "Értesítési engedély szükséges!";
            Log.e(TAG, "showNotification: " + errorMsg, e);
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
        }
    }
}
