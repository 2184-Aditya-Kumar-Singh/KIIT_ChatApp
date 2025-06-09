package com.example.kiit_chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiit_chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        loginUser(email, password);
}

    private void loginUser(String email, String password) {
        // Start login process after showing the loader for 2000ms
        new Handler().postDelayed(() -> {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Object bannedObj = snapshot.child("banned").getValue();
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
                                            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(LoadingActivity.this, "Error checking ban status. Try again.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Error retrieving user data.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoadingActivity.this, LoginActivity.class));
                            finish();
                        }
                    });
        }, 3000); // 2000ms = 2 seconds
    }


}