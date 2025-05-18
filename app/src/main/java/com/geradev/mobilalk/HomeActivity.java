package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    
    private TextInputEditText meterReadingEditText;
    private Button submitReadingButton;
    private FloatingActionButton profileFab;
    private RecyclerView readingsRecyclerView;
    private TextView noReadingsTextView;
    private View meterReadingCard;
    private TextView avgConsumptionTextView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ReadingsAdapter adapter;
    private String currentUserId;
    private ListenerRegistration readingsListener;    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
            return;
        }
        currentUserId = user.getUid();

        meterReadingEditText = findViewById(R.id.meterReadingEditText);
        submitReadingButton = findViewById(R.id.submitReadingButton);
        profileFab = findViewById(R.id.profileFab);
        readingsRecyclerView = findViewById(R.id.readingsRecyclerView);
        noReadingsTextView = findViewById(R.id.noReadingsTextView);
        meterReadingCard = findViewById(R.id.meterReadingCard);
        avgConsumptionTextView = findViewById(R.id.avgConsumptionTextView);

        adapter = new ReadingsAdapter();
        readingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        readingsRecyclerView.setAdapter(adapter);

        Animation cardAnimation = AnimationUtils.loadAnimation(this, R.anim.card_animation);
        meterReadingCard.startAnimation(cardAnimation);

        submitReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReading();
            }
        });        profileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
        
        // Menü beállítása
        findViewById(R.id.settingsButton).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });

        findViewById(R.id.calculateStatsButton).setOnClickListener(v -> {
            Log.d(TAG, "onClick: Statisztikai gomb megnyomva");
            calculateAverageConsumption();
        });

        setupRealtimeReadingsListener();
    }
      @Override
    protected void onResume() {
        super.onResume();
        setupRealtimeReadingsListener();
    }    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readingsListener != null) {
            readingsListener.remove();
        }
    }    private void setupRealtimeReadingsListener() {
        if (readingsListener != null) {
            readingsListener.remove();
        }
        readingsListener = db.collection("readings")
                .whereEqualTo("userId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(20) // Max. 20 bejegyzés lekérdezése
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, 
                                        @Nullable FirebaseFirestoreException e) {                        if (e != null) {
                            String errorMsg = "Hiba történt az adatok betöltése közben: " + e.getMessage();
                            Log.e(TAG, errorMsg, e);
                            Toast.makeText(HomeActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<MeterReading> readings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            MeterReading reading = document.toObject(MeterReading.class);
                            reading.setId(document.getId());
                            readings.add(reading);
                        }

                        adapter.setReadings(readings);

                        if (readings.isEmpty()) {
                            readingsRecyclerView.setVisibility(View.GONE);
                            noReadingsTextView.setVisibility(View.VISIBLE);
                        } else {
                            readingsRecyclerView.setVisibility(View.VISIBLE);
                            noReadingsTextView.setVisibility(View.GONE);
                        }
                    }
                });
    }    
    private void queryHighConsumptionReadings() {
        db.collection("readings")
            .whereEqualTo("userId", currentUserId)
            .whereGreaterThan("reading", 10000) // Magas fogyasztás (pl. 10000 kWh fölött)
            .orderBy("reading", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Feldolgozás szükség szerint
            });
    }
      private void queryReadingsByDateRange(Date startDate, Date endDate) {
        db.collection("readings")
            .whereEqualTo("userId", currentUserId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Feldolgozás szükség szerint
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_settings).setTitle(R.string.logout);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitReading() {
        String readingStr = meterReadingEditText.getText().toString().trim();
        if (readingStr.isEmpty()) {
            meterReadingEditText.setError("Óraállás megadása kötelező");
            meterReadingEditText.requestFocus();
            return;
        }

        double reading;
        try {
            reading = Double.parseDouble(readingStr);
        } catch (NumberFormatException e) {
            meterReadingEditText.setError("Érvénytelen érték");
            meterReadingEditText.requestFocus();
            return;
        }

        MeterReading meterReading = new MeterReading(reading, new Date(), currentUserId);

        db.collection("readings")
                .add(meterReading)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {                        if (task.isSuccessful()) {
                            String successMsg = "Óraállás sikeresen rögzítve: " + task.getResult().getId();
                            Log.d(TAG, successMsg);
                            Toast.makeText(HomeActivity.this, "Óraállás sikeresen rögzítve", Toast.LENGTH_SHORT).show();
                            meterReadingEditText.setText("");
                        } else {
                            String errorMsg = "Hiba történt a mérőállás rögzítésekor: " + task.getException().getMessage();
                            Log.e(TAG, errorMsg, task.getException());
                            Toast.makeText(HomeActivity.this, "Hiba történt: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadReadings() {
        db.collection("readings")
                .whereEqualTo("userId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<MeterReading> readings = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MeterReading reading = document.toObject(MeterReading.class);
                                reading.setId(document.getId());
                                readings.add(reading);
                            }

                            adapter.setReadings(readings);

                            if (readings.isEmpty()) {
                                readingsRecyclerView.setVisibility(View.GONE);
                                noReadingsTextView.setVisibility(View.VISIBLE);
                            } else {
                                readingsRecyclerView.setVisibility(View.VISIBLE);
                                noReadingsTextView.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Hiba történt az adatok betöltése közben",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }    private void calculateAverageConsumption() {
        Log.d(TAG, "calculateAverageConsumption: Átlagos fogyasztás számítása");
        db.collection("readings")
            .whereEqualTo("userId", currentUserId)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty() || queryDocumentSnapshots.size() < 2) {
                    String message = "Legalább két mérőállás szükséges a fogyasztás számításához";
                    Log.w(TAG, "calculateAverageConsumption: " + message);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                List<MeterReading> readings = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    MeterReading reading = document.toObject(MeterReading.class);
                    reading.setId(document.getId());
                    readings.add(reading);
                }
                
                // Kiszámoljuk az első és utolsó leolvasás közötti különbséget
                MeterReading firstReading = readings.get(0);
                MeterReading lastReading = readings.get(readings.size() - 1);
                
                double consumptionDiff = lastReading.getReading() - firstReading.getReading();
                
                // Kiszámoljuk a napok számát
                long timeDiffMillis = lastReading.getDate().getTime() - firstReading.getDate().getTime();
                int daysDiff = Math.max(1, (int) (timeDiffMillis / (1000 * 60 * 60 * 24)));
                
                // Átlagos napi fogyasztás
                double avgDailyConsumption = consumptionDiff / daysDiff;
                
                String resultMsg = String.format("Átlagos napi fogyasztás: %.2f kWh/nap", avgDailyConsumption);
                Log.d(TAG, "calculateAverageConsumption: " + resultMsg);
                
                // Megjelenítjük az eredményt
                avgConsumptionTextView.setText(resultMsg);
                avgConsumptionTextView.setVisibility(View.VISIBLE);
                
                // Animáljuk az eredményt
                Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                avgConsumptionTextView.startAnimation(fadeIn);
            })
            .addOnFailureListener(e -> {
                String errorMsg = "Hiba a fogyasztás számításakor: " + e.getMessage();
                Log.e(TAG, errorMsg, e);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            });
    }
}