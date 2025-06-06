package com.example.kiit_chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.activities.MainActivity;
import com.example.kiit_chatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSetupActivity";
    private EditText etName, etBranch, etYear;
    private CheckBox cbML, cbWebDev, cbAndroid, cbDataScience,cbCyberSecurity,cbCompetitiveProgramming;
    private Button btnSaveProfile;

    private DatabaseReference usersRef;
    private String currentUserId;
    private String currentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        etName = findViewById(R.id.etName);
        etBranch = findViewById(R.id.etBranch);
        etYear = findViewById(R.id.etYear);

        cbML = findViewById(R.id.cbML);
        cbWebDev = findViewById(R.id.cbWebDev);
        cbAndroid = findViewById(R.id.cbAndroid);
        cbDataScience = findViewById(R.id.cbDataScience);
        cbCyberSecurity=findViewById(R.id.cbCyberSecurity);
        cbCompetitiveProgramming=findViewById(R.id.cbCompetitiveProgramming);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Check authentication
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();
        currentUserEmail = auth.getCurrentUser().getEmail();

        // Initialize Realtime Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String year = etYear.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Please enter your name");
            return;
        }

        if (TextUtils.isEmpty(branch)) {
            etBranch.setError("Please enter your branch");
            return;
        }

        if (TextUtils.isEmpty(year)) {
            etYear.setError("Please enter your year");
            return;
        }

        List<String> interests = new ArrayList<>();
        if (cbML.isChecked()) interests.add("Machine Learning");
        if (cbWebDev.isChecked()) interests.add("Web Development");
        if (cbAndroid.isChecked()) interests.add("Android Development");
        if (cbDataScience.isChecked()) interests.add("Data Science");
        if (cbCyberSecurity.isChecked()) interests.add("Cyber Security");
        if (cbCompetitiveProgramming.isChecked()) interests.add("Competitive Programming");

        if (interests.isEmpty()) {
            Toast.makeText(this, "Please select at least one interest", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add loading state
        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        // Determine role based on email
        String emailPrefix = currentUserEmail != null ? currentUserEmail.split("@")[0] : "";
        String role = emailPrefix.matches("\\d+") ? "student" : "teacher";

        User user = new User(currentUserId, name, branch, year, interests, currentUserEmail, role);

        Log.d(TAG, "Saving user profile for: " + currentUserId);

        usersRef.child(currentUserId).setValue(user)
                .addOnCompleteListener(task -> {
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Profile");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Profile saved successfully");
                        Toast.makeText(ProfileSetupActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to MainActivity
                        Intent intent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Failed to save profile", task.getException());
                        Toast.makeText(ProfileSetupActivity.this, "Failed to save profile: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}