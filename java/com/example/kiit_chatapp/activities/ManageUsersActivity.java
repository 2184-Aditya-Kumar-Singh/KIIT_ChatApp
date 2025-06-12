package com.example.kiit_chatapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.adapters.UserManageAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageUsersActivity extends AppCompatActivity implements UserManageAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private EditText searchInput;
    private UserManageAdapter adapter;
    private List<Map<String, Object>> userList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        searchInput = findViewById(R.id.editTextSearchEmail);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserManageAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnSearchUser).setOnClickListener(v -> searchUser());
    }

    private void searchUser() {
        String email = searchInput.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter email to search", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> user = doc.getData();
                        if (user != null) {
                            user.put("userId", doc.getId());
                            userList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch user", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBanClick(int position) {
        String userId = (String) userList.get(position).get("userId");
        db.collection("users").document(userId)
                .update("banned", true)
                .addOnSuccessListener(unused -> Toast.makeText(this, "User banned", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to ban", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDeleteClick(int position) {
        String userId = (String) userList.get(position).get("userId");
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(unused -> {
                    userList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show());
    }
}
