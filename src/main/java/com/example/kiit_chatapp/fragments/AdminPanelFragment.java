package com.example.kiit_chatapp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.adapters.AdminUserAdapter;
import com.example.kiit_chatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPanelFragment extends Fragment {

    private Button btnAddGroup, btnManageUsers;
    private RecyclerView rvGroups, rvUsers;
    private GroupsAdapter groupsAdapter;
    private AdminUserAdapter userAdapter;
    private List<GroupModel> groupList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    private DatabaseReference groupsRef, usersRef;
    private String currentUserId;
    private TextView tvUsersLabel, groupListTitle;

    private static final List<String> ALL_INTERESTS = Arrays.asList(
            "Machine Learning", "Web Development", "Android Development", "Data Science", "Cyber Security", "Competitive Programming"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_panel, container, false);

        btnAddGroup = view.findViewById(R.id.btnAddGroup);
        btnManageUsers = view.findViewById(R.id.btnManageMember);
        rvGroups = view.findViewById(R.id.rvGroups);
        rvUsers = view.findViewById(R.id.rvUsers);
        tvUsersLabel = view.findViewById(R.id.tvUsersLabel);
        groupListTitle = view.findViewById(R.id.groupListTitle);

        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter = new GroupsAdapter(groupList, this::showGroupMembers, this::removeGroup);
        rvGroups.setAdapter(groupsAdapter);

        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new AdminUserAdapter(getContext(), userList, this::banUser, this::unbanUser);
        rvUsers.setAdapter(userAdapter);

        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        checkAdmin();

        btnAddGroup.setOnClickListener(v -> {
            // Show group UI, hide user UI
            tvUsersLabel.setVisibility(View.GONE);
            rvUsers.setVisibility(View.GONE);
            groupListTitle.setVisibility(View.VISIBLE);
            rvGroups.setVisibility(View.VISIBLE);
            showAddGroupDialog();
        });

        btnManageUsers.setOnClickListener(v -> {
            // Show user UI, hide group UI
            tvUsersLabel.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.VISIBLE);
            groupListTitle.setVisibility(View.GONE);
            rvGroups.setVisibility(View.GONE);
            loadUsers();
        });

        return view;
    }

    private void checkAdmin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !"22052184@kiit.ac.in".equalsIgnoreCase(user.getEmail())) {
            Toast.makeText(getContext(), "Access Denied: Not an admin", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().onBackPressed();
        } else {
            loadGroups();
        }
    }

    private void loadGroups() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupList.clear();
                for (DataSnapshot groupSnap : snapshot.getChildren()) {
                    GroupModel group = groupSnap.getValue(GroupModel.class);
                    if (group != null) {
                        group.setId(groupSnap.getKey());
                        groupList.add(group);
                    }
                }
                groupsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    Map<String, Object> userMap = (Map<String, Object>) userSnap.getValue();
                    User user = new User();
                    user.setUid(userMap.get("uid") != null ? userMap.get("uid").toString() : "");
                    user.setName(userMap.get("name") != null ? userMap.get("name").toString() : "");
                    user.setEmail(userMap.get("email") != null ? userMap.get("email").toString() : "");
                    Object bannedObj = userMap.get("banned");
                    if (bannedObj instanceof Boolean) {
                        user.setBanned((Boolean) bannedObj);
                    } else if (bannedObj instanceof String) {
                        user.setBanned(Boolean.parseBoolean((String) bannedObj));
                    } else {
                        user.setBanned(false);
                    }
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void banUser(User user) {
        if (user.getUid() == null) {
            Log.e("BAN", "User UID is null!");
            return;
        }
        usersRef.child(user.getUid()).child("banned").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Log.d("BAN", "User banned: " + user.getUid());
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Log.e("BAN", "Failed to ban user " + user.getUid(), e);
                });
    }

    private void unbanUser(User user) {
        usersRef.child(user.getUid()).child("banned").setValue(false)
                .addOnSuccessListener(aVoid -> loadUsers());
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Group");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText etGroupName = new EditText(getContext());
        etGroupName.setHint("Group Name");
        layout.addView(etGroupName);

        final Spinner interestsSpinner = new Spinner(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ALL_INTERESTS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interestsSpinner.setAdapter(adapter);
        layout.addView(interestsSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String groupName = etGroupName.getText().toString().trim();
            String selectedInterest = (String) interestsSpinner.getSelectedItem();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(getContext(), "Enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> groupMap = new HashMap<>();
            groupMap.put("name", groupName);
            groupMap.put("interests", Arrays.asList(selectedInterest));
            groupsRef.push().setValue(groupMap);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void removeGroup(GroupModel group) {
        groupsRef.child(group.getId()).removeValue();
    }

    private void showGroupMembers(GroupModel group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Members of " + group.getName());

        final List<String> memberNames = new ArrayList<>();
        final List<String> memberUids = new ArrayList<>();

        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            builder.setMessage("No members in this group.");
            builder.setPositiveButton("Add Member", (dialog, which) -> showAddMemberDialog(group));
            builder.setNegativeButton("Close", null);
            builder.show();
            return;
        }

        for (String uid : group.getMembers()) {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    memberNames.add(name != null ? name : uid);
                    memberUids.add(uid);
                    if (memberNames.size() == group.getMembers().size()) {
                        builder.setItems(memberNames.toArray(new String[0]), (dialog, which) -> {
                            showRemoveMemberDialog(group, memberUids.get(which));
                        });
                        builder.setPositiveButton("Add Member", (dialog, which) -> showAddMemberDialog(group));
                        builder.setNegativeButton("Close", null);
                        builder.show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void showAddMemberDialog(GroupModel group) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add User to " + group.getName());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText etEmail = new EditText(getContext());
        etEmail.setHint("Enter KIIT email of user");
        layout.addView(etEmail);

        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            if (TextUtils.isEmpty(email) || !email.endsWith("@kiit.ac.in")) {
                Toast.makeText(getContext(), "Enter valid KIIT email", Toast.LENGTH_SHORT).show();
                return;
            }
            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String uid = userSnap.getKey();
                            if (group.getMembers() == null) group.setMembers(new ArrayList<>());
                            if (!group.getMembers().contains(uid)) {
                                group.getMembers().add(uid);
                                groupsRef.child(group.getId()).child("members").setValue(group.getMembers());
                                Toast.makeText(getContext(), "User added to group!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "User already in group", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showRemoveMemberDialog(GroupModel group, String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Remove this user from group?");
        builder.setPositiveButton("Remove", (dialog, which) -> {
            if (group.getMembers() != null && group.getMembers().contains(uid)) {
                group.getMembers().remove(uid);
                groupsRef.child(group.getId()).child("members").setValue(group.getMembers());
                Toast.makeText(getContext(), "User removed from group", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    public static class GroupModel {
        private String id;
        private String name;
        private List<String> interests;
        private List<String> members;

        public GroupModel() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
        public List<String> getMembers() { return members; }
        public void setMembers(List<String> members) { this.members = members; }
    }

    public static class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
        private List<GroupModel> groups;
        private GroupActionListener onViewMembers, onDelete;

        public GroupsAdapter(List<GroupModel> groups, GroupActionListener onViewMembers, GroupActionListener onDelete) {
            this.groups = groups;
            this.onViewMembers = onViewMembers;
            this.onDelete = onDelete;
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            GroupModel group = groups.get(position);
            holder.bind(group, onViewMembers, onDelete);
        }

        @Override
        public int getItemCount() { return groups.size(); }

        static class GroupViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvGroupName, tvInterest;
            private final Button btnMembers, btnDelete;

            public GroupViewHolder(@NonNull View itemView) {
                super(itemView);
                tvGroupName = itemView.findViewById(R.id.tvGroupName);
                tvInterest = itemView.findViewById(R.id.tvInterest);
                btnMembers = itemView.findViewById(R.id.btnMembers);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }

            void bind(GroupModel group, GroupActionListener onViewMembers, GroupActionListener onDelete) {
                tvGroupName.setText(group.getName());
                tvInterest.setText("Interests: " + (group.getInterests() != null ? group.getInterests().toString() : ""));
                btnMembers.setOnClickListener(v -> onViewMembers.onAction(group));
                btnDelete.setOnClickListener(v -> onDelete.onAction(group));
            }
        }

        interface GroupActionListener {
            void onAction(GroupModel group);
        }
    }
}