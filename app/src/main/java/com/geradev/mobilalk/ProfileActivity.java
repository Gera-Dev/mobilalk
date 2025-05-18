package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int LOCATION_PERMISSION_CODE = 103;

    private TextView userEmailTextView;
    private ImageView profileImageView;
    private Button logoutButton;
    private Button capturePhotoButton;
    private Button getLocationButton;
    private Button backButton;
    private TextView locationTextView;
    private MaterialCardView profileCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Firebase inicializálás
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // UI elemek inicializálása
        userEmailTextView = findViewById(R.id.userEmailTextView);
        profileImageView = findViewById(R.id.profileImageView);
        logoutButton = findViewById(R.id.logoutButton);
        capturePhotoButton = findViewById(R.id.capturePhotoButton);
        getLocationButton = findViewById(R.id.getLocationButton);
        backButton = findViewById(R.id.backButton);
        locationTextView = findViewById(R.id.locationTextView);
        profileCard = findViewById(R.id.profileCard);

        // Jelenlegi felhasználó ellenőrzése
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userEmailTextView.setText(user.getEmail());
        } else {
            navigateToLogin();
            return;
        }

        // Animáció a profil kártyára
        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
        profileCard.startAnimation(pulseAnimation);

        // Gombok eseménykezelői
        logoutButton.setOnClickListener(v -> logoutUser());
        capturePhotoButton.setOnClickListener(v -> checkCameraPermissions());
        getLocationButton.setOnClickListener(v -> checkLocationPermissions());
        backButton.setOnClickListener(v -> finish());
    }

    // Lifecycle hook - onStart: Ellenőrizze újra a felhasználói bejelentkezést
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
        }
    }    private void logoutUser() {
        mAuth.signOut();
        Log.d(TAG, "Felhasználó kijelentkezett");
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String locationText = "Jelenlegi pozíció: " + 
                                "Szélesség: " + location.getLatitude() + 
                                ", Hosszúság: " + location.getLongitude();
                            locationTextView.setText(locationText);
                            locationTextView.setVisibility(View.VISIBLE);                        } else {
                            String errorMsg = "Nem sikerült a helymeghatározás";
                            Log.w(TAG, errorMsg);
                            Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMsg = "Helymeghatározási hiba: " + e.getMessage();
                        Log.e(TAG, errorMsg, e);
                        Toast.makeText(ProfileActivity.this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Kamera engedély megadva");
                openCamera();
            } else {
                String errorMsg = "Kamera engedély szükséges";
                Log.w(TAG, errorMsg);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Helymeghatározási engedély megadva");
                getLocation();
            } else {
                String errorMsg = "Helymeghatározási engedély szükséges";
                Log.w(TAG, errorMsg);
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                profileImageView.setImageBitmap((android.graphics.Bitmap) data.getExtras().get("data"));
                Toast.makeText(this, "Kép rögzítve", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
