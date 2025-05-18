package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    
    private static final String CHANNEL_ID = "meter_reading_channel";
    private static final String NOTIFICATION_PREFS = "notification_preferences";
    private static final String REMINDER_ENABLED_KEY = "reminder_enabled";
    private static final String REMINDER_DAYS_KEY = "reminder_days";
    private static final String ALARM_ACTION = "com.geradev.mobilalk.ALARM_ACTION";
    
    private Switch notificationSwitch;
    private SeekBar frequencySeekBar;
    private TextView frequencyTextView;
    private Button saveButton;
    private Button backButton;
    private MaterialCardView settingsCard;
    
    private SharedPreferences sharedPreferences;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    
    private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ALARM_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "onReceive: Emlékeztető időzítő esemény fogadva");
                showReminderNotification();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        Log.d(TAG, "onCreate: SettingsActivity inicializálása");
        createNotificationChannel();
        
        notificationSwitch = findViewById(R.id.notificationSwitch);
        frequencySeekBar = findViewById(R.id.frequencySeekBar);
        frequencyTextView = findViewById(R.id.frequencyTextView);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        settingsCard = findViewById(R.id.settingsCard);
        
        sharedPreferences = getSharedPreferences(NOTIFICATION_PREFS, MODE_PRIVATE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        IntentFilter filter = new IntentFilter(ALARM_ACTION);
        registerReceiver(alarmReceiver, filter);
        Log.d(TAG, "onCreate: AlarmReceiver regisztrálva");
        
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up_animation);
        settingsCard.startAnimation(slideAnimation);
        
        boolean reminderEnabled = sharedPreferences.getBoolean(REMINDER_ENABLED_KEY, false);
        int reminderDays = sharedPreferences.getInt(REMINDER_DAYS_KEY, 30);
        Log.d(TAG, "onCreate: Beállítások betöltve. Emlékeztető " + 
                (reminderEnabled ? "bekapcsolva" : "kikapcsolva") + 
                ", gyakoriság: " + reminderDays + " nap");
        
        notificationSwitch.setChecked(reminderEnabled);
        frequencySeekBar.setProgress(reminderDays);
        updateFrequencyText(reminderDays);
        
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: Emlékeztető kapcsoló: " + isChecked);
                frequencySeekBar.setEnabled(isChecked);
            }
        });
        
        frequencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int days = Math.max(1, progress);
                Log.d(TAG, "onProgressChanged: Új gyakoriság: " + days + " nap");
                updateFrequencyText(days);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Mentés gomb megnyomva");
                saveSettings();
            }
        });
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Vissza gomb megnyomva");
                finish();
            }
        });
        
        frequencySeekBar.setEnabled(reminderEnabled);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: AlarmReceiver felszabadítása");
        unregisterReceiver(alarmReceiver);
    }
    
    private void updateFrequencyText(int days) {
        frequencyTextView.setText(String.format("Emlékeztető gyakorisága: %d nap", days));
    }
    
    private void saveSettings() {
        boolean isEnabled = notificationSwitch.isChecked();
        int days = Math.max(1, frequencySeekBar.getProgress());
        Log.d(TAG, "saveSettings: Beállítások mentése: emlékeztető " + 
                (isEnabled ? "bekapcsolva" : "kikapcsolva") + 
                ", gyakoriság: " + days + " nap");
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(REMINDER_ENABLED_KEY, isEnabled);
        editor.putInt(REMINDER_DAYS_KEY, days);
        editor.apply();
        
        if (isEnabled) {
            Log.d(TAG, "saveSettings: Időzítők beállítása " + days + " napos gyakorisággal");
            scheduleAlarm(days);
            Toast.makeText(this, "Emlékeztetők bekapcsolva", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "saveSettings: Időzítők leállítása");
            cancelAlarm();
            Toast.makeText(this, "Emlékeztetők kikapcsolva", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: Értesítési csatorna létrehozása");
            CharSequence name = "Mérőóra Értesítések";
            String description = "Mérőóra-leolvasási emlékeztetők";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "createNotificationChannel: Értesítési csatorna sikeresen létrehozva");
            } else {
                Log.e(TAG, "createNotificationChannel: Nem sikerült a NotificationManager elérése");
            }
        } else {
            Log.d(TAG, "createNotificationChannel: Android N vagy korábbi, nem kell csatornát létrehozni");
        }
    }
    
    private void scheduleAlarm(int days) {
        Intent intent = new Intent(ALARM_ACTION);
        alarmIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        Log.d(TAG, "scheduleAlarm: AlarmManager beállítása " + days + " napos ismétlődéssel, kezdés: " + 
                calendar.getTime().toString());
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY * days,
            alarmIntent
        );
        
        scheduleJob();
    }
    
    private void cancelAlarm() {
        Log.d(TAG, "cancelAlarm: AlarmManager leállítása");
        Intent intent = new Intent(ALARM_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
        
        cancelJob();
    }
    
    private void scheduleJob() {
        ComponentName componentName = new ComponentName(this, MeterReadingJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(123, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setPeriodic(24 * 60 * 60 * 1000) // 24 óra
            .build();
            
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(jobInfo);
        
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "scheduleJob: JobScheduler sikeresen beállítva");
        } else {
            Log.e(TAG, "scheduleJob: JobScheduler beállítása sikertelen");
        }
    }
    
    private void cancelJob() {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG, "cancelJob: JobScheduler leállítva");
    }
    
    private void showReminderNotification() {
        Log.d(TAG, "showReminderNotification: Mérőóra-leolvasási emlékeztető létrehozása");
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Mérőóra leolvasás")
            .setContentText("Ideje leolvasni a mérőórádat!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "showReminderNotification: Értesítés sikeresen megjelenítve");
        } catch (SecurityException e) {
            String errorMessage = "Értesítési engedély szükséges!";
            Log.e(TAG, "showReminderNotification: " + errorMessage, e);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
