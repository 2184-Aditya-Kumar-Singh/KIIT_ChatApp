package com.example.kiit_chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiit_chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        mAuth = FirebaseAuth.getInstance();
        String email = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        String password = getIntent().getStringExtra(LoginActivity.EXTRA_PASSWORD);

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Toast.makeText(this, "Email or password missing. Please try again.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Try Auth first, then Firestore!
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            if (!firebaseUser.isEmailVerified()) {
                                mAuth.signOut();
                                Toast.makeText(LoadingActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                // Check "users" doc in Firestore
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                firestore.collection("users").document(firebaseUser.getUid())
                                        .get()
                                        .addOnCompleteListener(userTask -> {
                                            if (userTask.isSuccessful()) {
                                                DocumentSnapshot userDoc = userTask.getResult();
                                                if (userDoc != null && userDoc.exists()) {
                                                    // Now check Realtime Database for role
                                                    DatabaseReference realtimeDbRef = FirebaseDatabase.getInstance().getReference();
                                                    realtimeDbRef.child("users").child(firebaseUser.getUid()).child("role")
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (!dataSnapshot.exists() || dataSnapshot.getValue() == null) {
                                                                        // Role is null, do not allow login
                                                                        mAuth.signOut();
                                                                        Toast.makeText(LoadingActivity.this, "Complete the registration and save profile", Toast.LENGTH_LONG).show();
                                                                        startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                                                        finish();
                                                                    } else {
                                                                        // Check banned status from Firestore
                                                                        Object bannedObj = userDoc.get("banned");
                                                                        boolean isBanned = false;
                                                                        if (bannedObj instanceof Boolean) {
                                                                            isBanned = (Boolean) bannedObj;
                                                                        } else if (bannedObj instanceof String) {
                                                                            isBanned = Boolean.parseBoolean((String) bannedObj);
                                                                        }
                                                                        if (isBanned) {
                                                                            mAuth.signOut();
                                                                            Toast.makeText(LoadingActivity.this, "You have been banned. Contact admin.", Toast.LENGTH_LONG).show();
                                                                            startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                                                            finish();
                                                                        } else {
                                                                            // Show loader for 4.5s before MainActivity
                                                                            new Handler().postDelayed(() -> {
                                                                                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }, 4500);
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                    mAuth.signOut();
                                                                    Toast.makeText(LoadingActivity.this, "Error checking profile completion. Try again.", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                                                    finish();
                                                                }
                                                            });
                                                } else {
                                                    mAuth.signOut();
                                                    Toast.makeText(LoadingActivity.this, "Please register your account first.", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                                    finish();
                                                }
                                            } else {
                                                Exception ex = userTask.getException();
                                                String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
                                                ex.printStackTrace();
                                                mAuth.signOut();
                                                Toast.makeText(LoadingActivity.this, "Could not check user data. Please check your connection and try again.\n" + errorMsg, Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        Exception ex = authTask.getException();
                        String errorMsg = ex != null ? ex.getMessage() : "Unknown error";
                        ex.printStackTrace();
                        Toast.makeText(this, "Authentication failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }
}