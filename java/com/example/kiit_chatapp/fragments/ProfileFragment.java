package com.example.kiit_chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.activities.LoginActivity;
import com.example.kiit_chatapp.adapters.InterestAdapter;
import com.example.kiit_chatapp.models.InterestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private EditText etName, etBranch, etYear;
    private TextView tvEmail, tvRole, branch;
    private Button btnUpdate, btnLogout;
    private RecyclerView rvInterests;
    private InterestAdapter interestAdapter;

    private DatabaseReference usersRef;
    private String currentUserId;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etBranch = view.findViewById(R.id.etBranch);
        etYear = view.findViewById(R.id.etYear);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnLogout = view.findViewById(R.id.btnLogout);
        rvInterests = view.findViewById(R.id.rvInterests);
        branch = view.findViewById(R.id.labelBranch);

        // Set up RecyclerView for interests
        rvInterests.setLayoutManager(new LinearLayoutManager(getContext()));
        List<InterestModel> interestList = new ArrayList<>();
        for (String interest : ALL_INTERESTS) {
            interestList.add(new InterestModel(interest, false));
        }
        interestAdapter = new InterestAdapter(interestList);
        rvInterests.setAdapter(interestAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Force logout if not authenticated
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
            return view;
        }
        currentUserId = user.getUid();
        tvEmail.setText(user.getEmail());

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // --- Immediate UI update based on email prefix (before DB fetch) ---
        if (user.getEmail() != null && user.getEmail().endsWith("@kiit.ac.in")) {
            String emailPrefix = user.getEmail().split("@")[0];
            if (emailPrefix.matches(".*[a-zA-Z].*")) {
                etBranch.setHint("Department");
                branch.setText("Department");
                etYear.setVisibility(View.GONE);
            } else {
                etBranch.setHint("Branch");
                etYear.setVisibility(View.VISIBLE);
            }
        }

        loadProfile();

        btnUpdate.setOnClickListener(v -> updateProfile());
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });

        return view;
    }

    private void loadProfile() {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etBranch.setText(snapshot.child("branch").getValue(String.class));
                    etYear.setText(snapshot.child("year").getValue(String.class));
                    String role = snapshot.child("role").getValue(String.class);
                    tvRole.setText(role);

                    // Handle UI for teacher/student based on role from DB
                    if ("teacher".equalsIgnoreCase(role)) {
                        etBranch.setHint("Department");
                        branch.setText("Department");
                        etYear.setVisibility(View.GONE);
                    } else {
                        etBranch.setHint("Branch");
                        branch.setText("Branch");
                        etYear.setVisibility(View.VISIBLE);
                    }

                    // Get selected interests from DB
                    List<String> interests = new ArrayList<>();
                    if (snapshot.child("interests").exists()) {
                        for (DataSnapshot interestSnap : snapshot.child("interests").getChildren()) {
                            String interest = interestSnap.getValue(String.class);
                            interests.add(interest);
                        }
                    }

                    // Build a new list with checked/unchecked state
                    List<InterestModel> updatedList = new ArrayList<>();
                    for (String interest : ALL_INTERESTS) {
                        boolean selected = interests.contains(interest);
                        updatedList.add(new InterestModel(interest, selected));
                    }
                    interestAdapter.setInterests(updatedList); // This method should be added in your adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String branch = etBranch.getText().toString().trim();
        String year = etYear.getText().toString().trim();

        List<String> selectedInterests = new ArrayList<>();
        for (InterestModel model : interestAdapter.getInterests()) {
            if (model.isSelected()) selectedInterests.add(model.getName());
        }

        if (TextUtils.isEmpty(name)) {
            etName.setError("Please enter your name");
            return;
        }
        if (TextUtils.isEmpty(branch)) {
            etBranch.setError(etBranch.getHint() != null ? "Please enter your " + etBranch.getHint().toString().toLowerCase() : "Please enter this field");
            return;
        }
        // Only check year if it's visible
        if (etYear.getVisibility() == View.VISIBLE && TextUtils.isEmpty(year)) {
            etYear.setError("Please enter your year");
            return;
        }
        if (selectedInterests.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one interest", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", name);
        updateMap.put("branch", branch);
        updateMap.put("year", etYear.getVisibility() == View.VISIBLE ? year : "");
        updateMap.put("interests", selectedInterests);

        usersRef.child(currentUserId).updateChildren(updateMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}