package com.example.kiit_chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.activities.MainActivity;
import com.example.kiit_chatapp.adapters.InterestAdapter;
import com.example.kiit_chatapp.models.InterestModel;
import com.example.kiit_chatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProfileSetupActivity extends AppCompatActivity {

    private static final String TAG = "ProfileSetupActivity";
    private EditText etName, etBranch, etYear;
    private Button btnSaveProfile;
    private RecyclerView rvInterests;
    private InterestAdapter interestAdapter;

    private DatabaseReference usersRef;
    private String currentUserId;
    private String currentUserEmail;

    // Update this list if you want to add more interests
    private static final String[] ALL_INTERESTS = new String[]{
            "Machine Learning",
            "Web Development",
            "Android Development",
            "Data Science",
            "Cyber Security",
            "Competitive Programming",
            "Aptitude",
            "Interviews",
            "Game Development",
            "Robotics",
            "BlockChain",
            "Cloud Computing",
            "Data Structures",
            "Emotional Support"
            // Add more here if needed
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        etName = findViewById(R.id.etName);
        etBranch = findViewById(R.id.etBranch);
        etYear = findViewById(R.id.etYear);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        rvInterests = findViewById(R.id.rvInterests);

        // Setup RecyclerView for interests
        rvInterests.setLayoutManager(new LinearLayoutManager(this));
        List<InterestModel> interestList = new ArrayList<>();
        for (String interest : ALL_INTERESTS) {
            interestList.add(new InterestModel(interest, false));
        }
        interestAdapter = new InterestAdapter(interestList);
        rvInterests.setAdapter(interestAdapter);

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

        // --- New: Adapt UI for teacher or student ---
        if (currentUserEmail != null && currentUserEmail.endsWith("@kiit.ac.in")) {
            String emailPrefix = currentUserEmail.split("@")[0];
            // If emailPrefix contains any letter, treat as teacher
            if (emailPrefix.matches(".*[a-zA-Z].*")) {
                // It's a teacher
                etBranch.setHint("Department");
                etBranch.setText(""); // Optionally clear any previous text
                etYear.setVisibility(android.view.View.GONE);
            } else {
                // It's a student
                etBranch.setHint("Branch");
                etYear.setVisibility(android.view.View.VISIBLE);
            }
        }
        // --- End New ---

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
            etBranch.setError(etBranch.getHint() != null ? "Please enter your " + etBranch.getHint().toString().toLowerCase() : "Please enter this field");
            return;
        }

        // Only check year if it's visible (i.e., student)
        if (etYear.getVisibility() == android.view.View.VISIBLE && TextUtils.isEmpty(year)) {
            etYear.setError("Please enter your year");
            return;
        }

        List<String> interests = new ArrayList<>();
        for (InterestModel model : interestAdapter.getInterests()) {
            if (model.isSelected()) interests.add(model.getName());
        }

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

        // For teacher, you can save year as empty string or null
        User user = new User(currentUserId, name, branch, (etYear.getVisibility() == android.view.View.VISIBLE ? year : ""), interests, currentUserEmail, role);

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