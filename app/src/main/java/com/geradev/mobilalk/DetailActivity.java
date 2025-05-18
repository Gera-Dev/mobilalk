package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    
    private static final String TAG = "DetailActivity";
    
    private TextView readingValueTextView;
    private TextView readingDateTextView;
    private Button editButton;
    private Button deleteButton;
    private Button backButton;
    private MaterialCardView detailCard;
    
    private FirebaseFirestore db;
    private String readingId;
    private MeterReading currentReading;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        
        // Firebase inicializálás
        db = FirebaseFirestore.getInstance();
        
        // UI elemek inicializálása
        readingValueTextView = findViewById(R.id.readingValueTextView);
        readingDateTextView = findViewById(R.id.readingDateTextView);
        editButton = findViewById(R.id.editButton);
        deleteButton = findViewById(R.id.deleteButton);
        backButton = findViewById(R.id.backButton);
        detailCard = findViewById(R.id.detailCard);
        
        // Intent-ből adatok kinyerése
        readingId = getIntent().getStringExtra("READING_ID");
        
        // Animáció alkalmazása a kártyára
        Animation slideInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_animation);
        detailCard.startAnimation(slideInAnimation);
          if (readingId != null && !readingId.isEmpty()) {
            loadReadingDetails();
        } else {
            String errorMsg = "Hiba: Nincs olvasási azonosító";
            Log.e(TAG, errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            finish();
        }
        
        // Gombokhoz eseménykezelők hozzáadása
        backButton.setOnClickListener(v -> finish());
        
        editButton.setOnClickListener(v -> {
            if (currentReading != null) {
                Intent intent = new Intent(DetailActivity.this, EditActivity.class);
                intent.putExtra("READING_ID", readingId);
                intent.putExtra("READING_VALUE", currentReading.getReading());
                startActivity(intent);
            }
        });
        
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (readingId != null && !readingId.isEmpty()) {
            loadReadingDetails(); // Frissítés, ha az adatok változtak
        }
    }
    
    private void loadReadingDetails() {
        db.collection("readings")
            .document(readingId)
            .get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        currentReading = documentSnapshot.toObject(MeterReading.class);
                        currentReading.setId(documentSnapshot.getId());
                        
                        // Adatok megjelenítése
                        readingValueTextView.setText(String.format(Locale.getDefault(), "%.1f kWh", currentReading.getReading()));
                        
                        // Dátum formázása és megjelenítése
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.getDefault());
                        String formattedDate = dateFormat.format(currentReading.getDate());
                        readingDateTextView.setText(formattedDate);                    } else {
                        String errorMsg = "A leolvasás nem található: " + readingId;
                        Log.e(TAG, errorMsg);
                        Toast.makeText(DetailActivity.this, "A leolvasás nem található", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMsg = "Hiba a leolvasás betöltésekor: " + e.getMessage();
                    Log.e(TAG, errorMsg, e);
                    Toast.makeText(DetailActivity.this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }
    
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Leolvasás törlése")
                .setMessage("Biztosan törölni szeretnéd ezt a leolvasást?")
                .setPositiveButton("Törlés", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteReading();
                    }
                })
                .setNegativeButton("Mégsem", null)
                .show();
    }
    
    private void deleteReading() {
        db.collection("readings")
            .document(readingId)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {                @Override
                public void onSuccess(Void aVoid) {
                    String successMsg = "Leolvasás sikeresen törölve: " + readingId;
                    Log.d(TAG, successMsg);
                    Toast.makeText(DetailActivity.this, "Leolvasás sikeresen törölve", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMsg = "Törlési hiba (" + readingId + "): " + e.getMessage();
                    Log.e(TAG, errorMsg, e);
                    Toast.makeText(DetailActivity.this, "Törlési hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
