package com.geradev.mobilalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button registerButton;
    private ImageButton backButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        Log.d(TAG, "onCreate: RegisterActivity inicializálása");
        mAuth = FirebaseAuth.getInstance();

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Regisztrációs gomb megnyomva");
                registerUser();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Vissza gomb megnyomva");
                finish();
            }
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validálási folyamat
        if (name.isEmpty()) {
            Log.w(TAG, "registerUser: A név mező üres");
            nameEditText.setError("Név megadása kötelező");
            nameEditText.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            Log.w(TAG, "registerUser: Az email mező üres");
            emailEditText.setError("Email cím megadása kötelező");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            Log.w(TAG, "registerUser: A jelszó mező üres");
            passwordEditText.setError("Jelszó megadása kötelező");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            Log.w(TAG, "registerUser: A jelszó rövidebb, mint 6 karakter");
            passwordEditText.setError("A jelszónak legalább 6 karakter hosszúnak kell lennie");
            passwordEditText.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty() || !confirmPassword.equals(password)) {
            Log.w(TAG, "registerUser: A jelszavak nem egyeznek");
            confirmPasswordEditText.setError("A jelszavak nem egyeznek");
            confirmPasswordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "registerUser: Regisztrációs kísérlet: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Felhasználó sikeresen létrehozva");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            Log.d(TAG, "onComplete: Felhasználói profil frissítése: " + name);
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: Profil frissítése sikeres");
                                                Toast.makeText(RegisterActivity.this, "Sikeres regisztráció",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                String errorMessage = "Hiba történt a profil frissítése közben";
                                                Log.e(TAG, "onComplete: " + errorMessage, task.getException());
                                                Toast.makeText(RegisterActivity.this, errorMessage,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            String errorMessage = "A regisztráció sikertelen: " + 
                                (task.getException() != null ? task.getException().getMessage() : "ismeretlen hiba");
                            Log.e(TAG, "onComplete: Regisztrációs hiba", task.getException());
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}