package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
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

    private TextInputEditText meterReadingEditText;
    private Button submitReadingButton;
    private FloatingActionButton profileFab;
    private RecyclerView readingsRecyclerView;
    private TextView noReadingsTextView;
    private View meterReadingCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ReadingsAdapter adapter;
    private String currentUserId;
    private ListenerRegistration readingsListener;

    @Override
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
        });

        profileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Kijelentkezés...", Toast.LENGTH_SHORT).show();
                logoutUser();
            }
        });

        setupRealtimeReadingsListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readingsListener != null) {
            readingsListener.remove();
        }
    }

    private void setupRealtimeReadingsListener() {
        if (readingsListener != null) {
            readingsListener.remove();
        }

        readingsListener = db.collection("readings")
                .whereEqualTo("userId", currentUserId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, 
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(HomeActivity.this, "Hiba történt az adatok betöltése közben: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
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
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Óraállás sikeresen rögzítve", Toast.LENGTH_SHORT).show();
                            meterReadingEditText.setText("");
                        } else {
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
    }
}