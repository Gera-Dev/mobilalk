package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";
    
    private TextInputEditText editReadingEditText;
    private Button updateButton;
    private Button cancelButton;
    private MaterialCardView editCard;
    private TextView pageTitle;
    
    private FirebaseFirestore db;
    private String readingId;
    private double currentValue;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        
        // Firebase inicializálás
        db = FirebaseFirestore.getInstance();
        
        // UI elemek inicializálása
        editReadingEditText = findViewById(R.id.editReadingEditText);
        updateButton = findViewById(R.id.updateButton);
        cancelButton = findViewById(R.id.cancelButton);
        editCard = findViewById(R.id.editCard);
        pageTitle = findViewById(R.id.pageTitle);
        
        // Animáció alkalmazása a kártyára
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_animation);
        editCard.startAnimation(fadeInAnimation);
        
        // Intent-ből adatok kinyerése
        readingId = getIntent().getStringExtra("READING_ID");
        currentValue = getIntent().getDoubleExtra("READING_VALUE", 0.0);
        
        // Mező előzetes kitöltése
        editReadingEditText.setText(String.valueOf(currentValue));
        
        // Gombok eseménykezelőinek beállítása
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReading();
            }
        });
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    
    private void updateReading() {
        String readingString = editReadingEditText.getText().toString().trim();
        
        if (readingString.isEmpty()) {
            editReadingEditText.setError("Kérlek add meg az értéket");
            return;
        }
        
        double newReading;
        try {
            newReading = Double.parseDouble(readingString);
        } catch (NumberFormatException e) {
            editReadingEditText.setError("Érvénytelen érték");
            return;
        }
        
        if (newReading <= 0) {
            editReadingEditText.setError("Az értéknek nagyobbnak kell lennie nullánál");
            return;
        }
        
        DocumentReference readingRef = db.collection("readings").document(readingId);
          readingRef.update("reading", newReading)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    String successMsg = "Leolvasás sikeresen frissítve: " + readingId + ", új érték: " + newReading;
                    Log.d(TAG, successMsg);
                    Toast.makeText(EditActivity.this, "Leolvasás sikeresen frissítve", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMsg = "Frissítési hiba (" + readingId + "): " + e.getMessage();
                    Log.e(TAG, errorMsg, e);
                    Toast.makeText(EditActivity.this, "Frissítési hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
      // Lifecycle hook - onPause: Az aktuális állapot mentése SharedPreferences használatával
    @Override
    protected void onPause() {
        super.onPause();
        
        // Aktuális érték mentése
        String currentInput = editReadingEditText.getText().toString().trim();
        if (!currentInput.isEmpty()) {
            getSharedPreferences("edit_state", MODE_PRIVATE)
                .edit()
                .putString("last_value", currentInput)
                .putString("last_id", readingId)
                .apply();
            
            Log.d(TAG, "Szerkesztési állapot mentve: ID=" + readingId + ", Érték=" + currentInput);
        }
    }
    
    // Lifecycle hook - onResume: Az elmentett állapot visszaállítása
    @Override
    protected void onResume() {
        super.onResume();
        
        // Ha az aktuális képernyő ugyanarra az elemre vonatkozik, mint ami el volt mentve
        String savedId = getSharedPreferences("edit_state", MODE_PRIVATE).getString("last_id", "");
          if (savedId.equals(readingId)) {
            String savedValue = getSharedPreferences("edit_state", MODE_PRIVATE).getString("last_value", "");
            if (!savedValue.isEmpty()) {
                editReadingEditText.setText(savedValue);
                Log.d(TAG, "Szerkesztési állapot visszaállítva: ID=" + readingId + ", Érték=" + savedValue);
            }
        }
    }
}
