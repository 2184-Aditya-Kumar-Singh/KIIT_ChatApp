package com.example.kiit_chatapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.kiit_chatapp.adapters.GroupMemberAdapter;
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

public class AdminPanelFragment extends Fragment {

    private Button btnAddGroup, btnManageUsers, btnAddMember;
    private EditText etAddMemberEmail, etSearchUser;
    private LinearLayout layoutAddMember, searchBarContainer;
    private RecyclerView rvGroups, rvUsers;
    private TextView tvUsersLabel, groupListTitle;

    private GroupsAdapter groupsAdapter;
    private AdminUserAdapter userAdapter;
    private GroupMemberAdapter memberAdapter;

    private List<GroupModel> groupList = new ArrayList<>();
    private List<User> userList = new ArrayList<>();
    private List<User> fullUserList = new ArrayList<>();
    private List<User> memberList = new ArrayList<>();
    private DatabaseReference groupsRef, usersRef;
    private String currentUserId;

    // For tracking which group is being managed
    private GroupModel activeGroup = null;

    private static final List<String> ALL_INTERESTS = Arrays.asList(
            "Private","Machine Learning", "Web Development", "Android Development", "Data Science", "Cyber Security",
            "Competitive Programming", "Aptitude", "Interviews", "Game Development", "Robotics",
            "BlockChain", "Cloud Computing", "Data Structures", "Emotional Support"
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

        layoutAddMember = view.findViewById(R.id.layoutAddMember);
        etAddMemberEmail = view.findViewById(R.id.etAddMemberEmail);
        btnAddMember = view.findViewById(R.id.btnAddMember);

        searchBarContainer = view.findViewById(R.id.searchBarContainer);
        etSearchUser = view.findViewById(R.id.etSearchUser);

        // Adapters
        groupsAdapter = new GroupsAdapter(groupList, this::showGroupMembers, this::removeGroup);
        memberAdapter = new GroupMemberAdapter(memberList, this::kickMemberFromGroup);
        userAdapter = new AdminUserAdapter(getContext(), userList, this::banUser, this::unbanUser, this::makeAdmin, this::removeAdmin);

        rvGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        rvGroups.setAdapter(groupsAdapter);
        rvUsers.setAdapter(userAdapter);

        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        checkAdmin();

        // Initially hide add member bar and search bar
        layoutAddMember.setVisibility(View.GONE);
        searchBarContainer.setVisibility(View.GONE);

        btnAddGroup.setOnClickListener(v -> {
            exitMemberMode();
            tvUsersLabel.setVisibility(View.GONE);
            rvUsers.setVisibility(View.GONE);
            searchBarContainer.setVisibility(View.GONE);
            groupListTitle.setVisibility(View.VISIBLE);
            rvGroups.setVisibility(View.VISIBLE);
            showAddGroupDialog();
        });

        btnManageUsers.setOnClickListener(v -> {
            exitMemberMode();
            tvUsersLabel.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.VISIBLE);
            searchBarContainer.setVisibility(View.VISIBLE);
            groupListTitle.setVisibility(View.GONE);
            rvGroups.setVisibility(View.GONE);
            loadUsers();
        });

        btnAddMember.setOnClickListener(v -> {
            if (activeGroup == null) return;
            String email = etAddMemberEmail.getText().toString().trim().toLowerCase();
            if (TextUtils.isEmpty(email) || !email.endsWith("@kiit.ac.in")) {
                Toast.makeText(getContext(), "Enter valid KIIT email", Toast.LENGTH_SHORT).show();
                return;
            }
            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String uid = userSnap.getKey();
                        if (activeGroup.getMembers() == null) activeGroup.setMembers(new ArrayList<>());
                        if (activeGroup.getMembers().contains(uid)) {
                            Toast.makeText(getContext(), "User already in group", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        activeGroup.getMembers().add(uid);
                        groupsRef.child(activeGroup.getId()).child("members").setValue(activeGroup.getMembers());
                        Toast.makeText(getContext(), "User added to group!", Toast.LENGTH_SHORT).show();
                        etAddMemberEmail.setText("");
                        loadGroupMembers(activeGroup);
                        break;
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void checkAdmin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Access Denied: Not an admin", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().onBackPressed();
            return;
        }
        usersRef.child(user.getUid()).child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isAdmin = false;
                Object adminObj = snapshot.getValue();
                if (adminObj instanceof Boolean) {
                    isAdmin = (Boolean) adminObj;
                } else if (adminObj instanceof String) {
                    isAdmin = Boolean.parseBoolean((String) adminObj);
                }
                if (!isAdmin) {
                    Toast.makeText(getContext(), "Access Denied: Not an admin", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) getActivity().onBackPressed();
                } else {
                    loadGroups();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Access Denied: Not an admin", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });
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
                fullUserList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    if (!userSnap.exists()) continue;
                    User user = new User();
                    user.setUid(userSnap.child("uid").getValue(String.class));
                    user.setName(userSnap.child("name").getValue(String.class));
                    String email = userSnap.child("email").getValue(String.class);
                    user.setEmail(email);
                    if ("22052184@kiit.ac.in".equalsIgnoreCase(email)) continue;
                    Object bannedObj = userSnap.child("banned").getValue();
                    if (bannedObj instanceof Boolean) user.setBanned((Boolean) bannedObj);
                    else if (bannedObj instanceof String) user.setBanned(Boolean.parseBoolean((String) bannedObj));
                    else user.setBanned(false);
                    Object adminObj = userSnap.child("admin").getValue();
                    if (adminObj instanceof Boolean) user.setAdmin((Boolean) adminObj);
                    else if (adminObj instanceof String) user.setAdmin(Boolean.parseBoolean((String) adminObj));
                    else user.setAdmin(false);
                    userList.add(user);
                    fullUserList.add(user);
                }
                filterUsers(etSearchUser.getText().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterUsers(String query) {
        userList.clear();
        if (TextUtils.isEmpty(query)) {
            userList.addAll(fullUserList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User user : fullUserList) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(lowerQuery)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerQuery))) {
                    userList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    // GROUP MEMBERS MODE
    private void showGroupMembers(GroupModel group) {
        activeGroup = group;
        layoutAddMember.setVisibility(View.VISIBLE);
        groupListTitle.setText("Members of " + group.getName());
        rvGroups.setVisibility(View.VISIBLE);
        tvUsersLabel.setVisibility(View.GONE);
        rvUsers.setVisibility(View.GONE);
        searchBarContainer.setVisibility(View.GONE);
        loadGroupMembers(group);
    }

    private void loadGroupMembers(GroupModel group) {
        memberList.clear();
        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            memberAdapter = new GroupMemberAdapter(memberList, this::kickMemberFromGroup);
            rvGroups.setAdapter(memberAdapter);
            memberAdapter.notifyDataSetChanged();
            return;
        }
        final List<String> uids = group.getMembers();
        for (String uid : uids) {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) return;
                    User user = new User();
                    user.setUid(snapshot.child("uid").getValue(String.class));
                    user.setName(snapshot.child("name").getValue(String.class));
                    user.setEmail(snapshot.child("email").getValue(String.class));
                    Object bannedObj = snapshot.child("banned").getValue();
                    if (bannedObj instanceof Boolean) user.setBanned((Boolean) bannedObj);
                    else if (bannedObj instanceof String) user.setBanned(Boolean.parseBoolean((String) bannedObj));
                    else user.setBanned(false);
                    Object adminObj = snapshot.child("admin").getValue();
                    if (adminObj instanceof Boolean) user.setAdmin((Boolean) adminObj);
                    else if (adminObj instanceof String) user.setAdmin(Boolean.parseBoolean((String) adminObj));
                    else user.setAdmin(false);
                    memberList.add(user);
                    if (memberList.size() == uids.size()) {
                        memberAdapter = new GroupMemberAdapter(memberList, AdminPanelFragment.this::kickMemberFromGroup);
                        rvGroups.setAdapter(memberAdapter);
                        memberAdapter.notifyDataSetChanged();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void kickMemberFromGroup(User user) {
        if (activeGroup == null) return;
        if (activeGroup.getMembers() != null && activeGroup.getMembers().contains(user.getUid())) {
            activeGroup.getMembers().remove(user.getUid());
            groupsRef.child(activeGroup.getId()).child("members").setValue(activeGroup.getMembers());
            Toast.makeText(getContext(), "User removed from group", Toast.LENGTH_SHORT).show();
            loadGroupMembers(activeGroup);
        }
    }

    // To exit group members mode & return to group list
    private void exitMemberMode() {
        activeGroup = null;
        layoutAddMember.setVisibility(View.GONE);
        groupListTitle.setText("Group List");
        rvGroups.setAdapter(groupsAdapter);
        rvGroups.setVisibility(View.VISIBLE);
        groupsAdapter.notifyDataSetChanged();
    }

    private void banUser(User user) {
        if (user.getUid() == null) return;
        usersRef.child(user.getUid()).child("banned").setValue(true)
                .addOnSuccessListener(aVoid -> loadUsers());
    }

    private void unbanUser(User user) {
        usersRef.child(user.getUid()).child("banned").setValue(false)
                .addOnSuccessListener(aVoid -> loadUsers());
    }

    private void makeAdmin(User user) {
        if (user.getUid() == null) return;
        usersRef.child(user.getUid()).child("admin").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User is now Admin", Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
    }

    private void removeAdmin(User user) {
        if (user.getUid() == null) return;
        usersRef.child(user.getUid()).child("admin").setValue(false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User admin removed", Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
    }

    private void showAddGroupDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
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

    // --- GroupModel & GroupsAdapter definitions ---
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