package com.example.kiit_chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiit_chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button verifyEmailButton, registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser firebaseUser;

    private String emailInput, passwordInput, confirmPasswordInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        verifyEmailButton = findViewById(R.id.verifyEmailButton);
        registerButton = findViewById(R.id.registerButton);

        // Register button is disabled until email is verified and not already registered
        registerButton.setEnabled(false);

        verifyEmailButton.setOnClickListener(v -> sendVerificationEmail());

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void sendVerificationEmail() {
        emailInput = emailEditText.getText().toString().trim();
        passwordInput = passwordEditText.getText().toString().trim();
        confirmPasswordInput = confirmPasswordEditText.getText().toString().trim();

        if (!emailInput.endsWith("@kiit.ac.in")) {
            Toast.makeText(this, "Only @kiit.ac.in emails are allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(emailInput) || TextUtils.isEmpty(passwordInput) || TextUtils.isEmpty(confirmPasswordInput)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!passwordInput.equals(confirmPasswordInput)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Always try to create the user first
        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(createTask -> {
                    if (createTask.isSuccessful()) {
                        firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                            verifyEmailButton.setEnabled(false);
                                            registerButton.setEnabled(false);
                                            mAuth.signOut();
                                        } else {
                                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Exception e = createTask.getException();
                        String errorMsg = e != null ? e.getMessage() : "Unknown error";
                        String errorCode = (e instanceof FirebaseAuthException) ? ((FirebaseAuthException) e).getErrorCode() : "";

                        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(errorCode) ||
                                errorMsg.toLowerCase().contains("already in use")) {
                            // Email already in use: sign in and check verification and Firestore
                            mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                                    .addOnCompleteListener(signInTask -> {
                                        if (signInTask.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                user.reload().addOnCompleteListener(reloadTask -> {
                                                    if (user.isEmailVerified()) {
                                                        // Check Firestore before enabling register
                                                        db.collection("users").document(user.getUid())
                                                                .get()
                                                                .addOnSuccessListener(documentSnapshot -> {
                                                                    if (documentSnapshot.exists()) {
                                                                        // Now also check Realtime Database for role
                                                                        DatabaseReference realtimeDbRef = FirebaseDatabase.getInstance().getReference();
                                                                        realtimeDbRef.child("users").child(user.getUid()).child("role")
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        // If role is null, allow registration (enable button)
                                                                                        if (!dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                                                                            Toast.makeText(RegisterActivity.this, "Account verified. Please complete registration.", Toast.LENGTH_LONG).show();
                                                                                            registerButton.setEnabled(true);
                                                                                            verifyEmailButton.setEnabled(false);
                                                                                        } else {
                                                                                            // If role exists
                                                                                            Toast.makeText(RegisterActivity.this, "You are already registered. Please log in.", Toast.LENGTH_LONG).show();
                                                                                            registerButton.setEnabled(false);
                                                                                            verifyEmailButton.setEnabled(false);
                                                                                            mAuth.signOut();
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                                        Toast.makeText(RegisterActivity.this, "Error checking registration status. Try again.", Toast.LENGTH_SHORT).show();
                                                                                        registerButton.setEnabled(false);
                                                                                        verifyEmailButton.setEnabled(true);
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        // Firestore doc does not exist, treat as verified user but not registered
                                                                        Toast.makeText(RegisterActivity.this, "Account verified. Please complete registration.", Toast.LENGTH_LONG).show();
                                                                        registerButton.setEnabled(true);
                                                                        verifyEmailButton.setEnabled(false);
                                                                    }
                                                                });
                                                    } else {
                                                        // Not verified, send verification mail
                                                        user.sendEmailVerification()
                                                                .addOnCompleteListener(verifyTask -> {
                                                                    if (verifyTask.isSuccessful()) {
                                                                        Toast.makeText(this, "Verification email resent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                                                        verifyEmailButton.setEnabled(false);
                                                                        registerButton.setEnabled(false);
                                                                        mAuth.signOut();
                                                                    } else {
                                                                        Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                });
                                            }
                                        } else {
                                            Exception signInEx = signInTask.getException();
                                            String signInMsg = signInEx != null ? signInEx.getMessage() : "Unknown error";
                                            String signInCode = (signInEx instanceof FirebaseAuthException) ? ((FirebaseAuthException) signInEx).getErrorCode() : "";
                                            if ("ERROR_WRONG_PASSWORD".equals(signInCode) ||
                                                    signInMsg.toLowerCase().contains("password is invalid")) {
                                                Toast.makeText(this, "Wrong password for this account. Please log in.", Toast.LENGTH_LONG).show();
                                                registerButton.setEnabled(false);
                                                verifyEmailButton.setEnabled(false);
                                            } else if ("ERROR_USER_DISABLED".equals(signInCode)) {
                                                Toast.makeText(this, "This account has been disabled by admin.", Toast.LENGTH_LONG).show();
                                                registerButton.setEnabled(false);
                                                verifyEmailButton.setEnabled(false);
                                            } else {
                                                Toast.makeText(this, "Auth error: " + signInMsg, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Registration failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(emailInput) && !TextUtils.isEmpty(passwordInput)) {
            mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                firebaseUser.reload().addOnCompleteListener(reloadTask -> {
                                    if (firebaseUser.isEmailVerified()) {
                                        db.collection("users").document(firebaseUser.getUid())
                                                .get()
                                                .addOnSuccessListener(documentSnapshot -> {
                                                    if (documentSnapshot.exists()) {
                                                        // Now also check Realtime Database for role
                                                        DatabaseReference realtimeDbRef = FirebaseDatabase.getInstance().getReference();
                                                        realtimeDbRef.child("users").child(firebaseUser.getUid()).child("role")
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        // If role is null, allow registration (enable button)
                                                                        if (!dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                                                            registerButton.setEnabled(true);
                                                                            verifyEmailButton.setEnabled(false);
                                                                        } else {
                                                                            // If role exists, already registered
                                                                            registerButton.setEnabled(false);
                                                                            verifyEmailButton.setEnabled(false);
                                                                            Toast.makeText(RegisterActivity.this, "You are already registered. Please log in.", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {
                                                                        registerButton.setEnabled(false);
                                                                        verifyEmailButton.setEnabled(true);
                                                                        Toast.makeText(RegisterActivity.this, "Error checking registration status. Try again.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    } else {
                                                        // Firestore doc does not exist, treat as verified user but not registered
                                                        registerButton.setEnabled(true);
                                                        verifyEmailButton.setEnabled(false);
                                                    }
                                                });
                                    } else {
                                        registerButton.setEnabled(false);
                                        verifyEmailButton.setEnabled(true);
                                    }
                                });
                            }
                        } else {
                            registerButton.setEnabled(false);
                            verifyEmailButton.setEnabled(true);
                        }
                    });
        } else {
            registerButton.setEnabled(false);
            verifyEmailButton.setEnabled(true);
        }
    }

    private void registerUser() {
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", firebaseUser.getUid());
                            userMap.put("email", firebaseUser.getEmail());

                            db.collection("users").document(firebaseUser.getUid())
                                    .set(userMap)
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });

                            DatabaseReference realtimeDbRef = FirebaseDatabase.getInstance().getReference();
                            realtimeDbRef.child("users").child(firebaseUser.getUid())
                                    .setValue(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registration complete!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, ProfileSetupActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Realtime DB error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", firebaseUser.getUid());
                            userMap.put("email", firebaseUser.getEmail());

                            db.collection("users").document(firebaseUser.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        DatabaseReference realtimeDbRef = FirebaseDatabase.getInstance().getReference();
                                        realtimeDbRef.child("users").child(firebaseUser.getUid())
                                                .setValue(userMap)
                                                .addOnSuccessListener(aVoid2 -> {
                                                    Toast.makeText(this, "Registration complete!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterActivity.this, ProfileSetupActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Realtime DB error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        } else {
            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
        }
    }
}