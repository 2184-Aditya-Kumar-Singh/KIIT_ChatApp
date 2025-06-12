package com.example.kiit_chatapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kiit_chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";
    private EditText editGroupName,editDesc;
    private Button btnCreateGroup;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        editGroupName = findViewById(R.id.editGroupName);
        btnCreateGroup = findViewById(R.id.btn_create);
        editDesc=findViewById(R.id.edit_group_desc);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is authenticated
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String groupName = editGroupName.getText().toString().trim();
        String groupDesc = editDesc.getText().toString().trim();
        if (TextUtils.isEmpty(groupName)) {
            editGroupName.setError("Enter group name");
            return;
        }
        if (TextUtils.isEmpty(groupDesc)) {
            editDesc.setError("Enter description");
            return;
        }

        // Add loading indicator
        btnCreateGroup.setEnabled(false);
        btnCreateGroup.setText("Creating...");

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("groupName", groupName);
        groupData.put("groupDescription", groupDesc);

        Log.d(TAG, "Attempting to create group: " + groupName);

        db.collection("groups")
                .add(groupData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Group created with ID: " + documentReference.getId());
                    Toast.makeText(CreateGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                    btnCreateGroup.setEnabled(true);
                    btnCreateGroup.setText("Create Group");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating group", e);
                    Toast.makeText(CreateGroupActivity.this, "Failed to create group: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnCreateGroup.setEnabled(true);
                    btnCreateGroup.setText("Create Group");
                });
    }
}