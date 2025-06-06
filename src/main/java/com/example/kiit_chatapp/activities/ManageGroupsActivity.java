package com.example.kiit_chatapp.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.adapters.GroupAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageGroupsActivity extends AppCompatActivity implements GroupAdapter.OnGroupDeleteListener {

    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Map<String, Object>> groupList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups);

        recyclerView = findViewById(R.id.recyclerViewGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupList = new ArrayList<>();
        adapter = new GroupAdapter(groupList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadGroups();
    }

    private void loadGroups() {
        db.collection("groups")
                .get()
                .addOnSuccessListener(this::onGroupsLoaded)
                .addOnFailureListener(e -> Toast.makeText(ManageGroupsActivity.this, "Failed to load groups", Toast.LENGTH_SHORT).show());
    }

    private void onGroupsLoaded(QuerySnapshot queryDocumentSnapshots) {
        groupList.clear();

        for (DocumentSnapshot doc : queryDocumentSnapshots) {
            Map<String, Object> group = doc.getData();
            if (group != null) {
                group.put("groupId", doc.getId()); // Store doc ID for deletion
                groupList.add(group);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteGroupClicked(int position) {
        Map<String, Object> group = groupList.get(position);
        String groupId = (String) group.get("groupId");
        if (groupId == null) {
            Toast.makeText(this, "Invalid group ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("groups")
                .document(groupId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(ManageGroupsActivity.this, "Group deleted", Toast.LENGTH_SHORT).show();
                    groupList.remove(position);
                    adapter.notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> Toast.makeText(ManageGroupsActivity.this, "Failed to delete group", Toast.LENGTH_SHORT).show());
    }
}
