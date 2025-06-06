package com.example.kiit_chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.activities.LoginActivity;
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
    private CheckBox cbML, cbWebDev, cbAndroid, cbDataScience,cbCyberSecurity,cbCompetitiveProgramming;
    private TextView tvEmail, tvRole;
    private Button btnUpdate, btnLogout;

    private DatabaseReference usersRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etName);
        etBranch = view.findViewById(R.id.etBranch);
        etYear = view.findViewById(R.id.etYear);
        cbML = view.findViewById(R.id.cbML);
        cbWebDev = view.findViewById(R.id.cbWebDev);
        cbAndroid = view.findViewById(R.id.cbAndroid);
        cbDataScience = view.findViewById(R.id.cbDataScience);
        cbCyberSecurity=view.findViewById(R.id.cbCyberSecurity);
        cbCompetitiveProgramming=view.findViewById(R.id.cbCompetitiveProgramming);

        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);

        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnLogout = view.findViewById(R.id.btnLogout);
        Toast.makeText(getContext(), "ProfileFragment loaded", Toast.LENGTH_SHORT).show();
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
                    tvRole.setText(snapshot.child("role").getValue(String.class));

                    // Uncheck all, then set based on interests list
                    cbML.setChecked(false);
                    cbWebDev.setChecked(false);
                    cbAndroid.setChecked(false);
                    cbDataScience.setChecked(false);
                    cbCyberSecurity.setChecked(false);
                    cbCompetitiveProgramming.setChecked(false);

                    List<String> interests = new ArrayList<>();
                    if (snapshot.child("interests").exists()) {
                        for (DataSnapshot interestSnap : snapshot.child("interests").getChildren()) {
                            String interest = interestSnap.getValue(String.class);
                            interests.add(interest);
                        }
                    }
                    for (String interest : interests) {
                        if ("Machine Learning".equals(interest)) cbML.setChecked(true);
                        if ("Web Development".equals(interest)) cbWebDev.setChecked(true);
                        if ("Android Development".equals(interest)) cbAndroid.setChecked(true);
                        if ("Data Science".equals(interest)) cbDataScience.setChecked(true);
                        if ("Cyber Security".equals(interest)) cbCyberSecurity.setChecked(true);
                        if ("Competitive Programming".equals(interest)) cbCompetitiveProgramming.setChecked(true);
                    }
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

        List<String> interests = new ArrayList<>();
        if (cbML.isChecked()) interests.add("Machine Learning");
        if (cbWebDev.isChecked()) interests.add("Web Development");
        if (cbAndroid.isChecked()) interests.add("Android Development");
        if (cbDataScience.isChecked()) interests.add("Data Science");
        if (cbCyberSecurity.isChecked()) interests.add("Cyber Security");
        if (cbCompetitiveProgramming.isChecked()) interests.add("Competitive Programming");

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
        if (interests.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one interest", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", name);
        updateMap.put("branch", branch);
        updateMap.put("year", year);
        updateMap.put("interests", interests);

        usersRef.child(currentUserId).updateChildren(updateMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}