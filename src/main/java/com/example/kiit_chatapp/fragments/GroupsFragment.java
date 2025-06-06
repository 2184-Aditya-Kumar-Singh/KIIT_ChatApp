package com.example.kiit_chatapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerViewGroups;
    private GroupsAdapter groupsAdapter;
    private List<GroupModel> filteredGroups = new ArrayList<>();
    private Set<String> joinedGroupIds = new HashSet<>();
    private DatabaseReference usersRef, groupsRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerViewGroups = view.findViewById(R.id.recyclerViewGroups);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsAdapter = new GroupsAdapter(filteredGroups, joinedGroupIds, new GroupsAdapter.OnActionClickListener() {
            @Override
            public void onActionClick(GroupModel group, int position, boolean alreadyJoined) {
                if (!alreadyJoined) {
                    joinGroup(group, position);
                } else {
                    openGroupChat(group);
                }
            }
        });
        recyclerViewGroups.setAdapter(groupsAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        loadUserJoinedGroups();

        return view;
    }

    private void loadUserJoinedGroups() {
        usersRef.child(currentUserId).child("joinedGroups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                joinedGroupIds.clear();
                for (DataSnapshot groupSnap : snapshot.getChildren()) {
                    joinedGroupIds.add(groupSnap.getKey());
                }
                loadUserInterestsAndGroups();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load joined groups", Toast.LENGTH_SHORT).show();
                loadUserInterestsAndGroups();
            }
        });
    }

    private void loadUserInterestsAndGroups() {
        usersRef.child(currentUserId).child("interests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                Set<String> userInterests = new HashSet<>();
                for (DataSnapshot interestSnap : userSnapshot.getChildren()) {
                    String interest = interestSnap.getValue(String.class);
                    if (interest != null) userInterests.add(interest);
                }
                loadAndFilterGroups(userInterests);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load interests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAndFilterGroups(Set<String> userInterests) {
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                filteredGroups.clear();
                for (DataSnapshot groupSnap : groupsSnapshot.getChildren()) {
                    GroupModel group = groupSnap.getValue(GroupModel.class);
                    group.setId(groupSnap.getKey());
                    if (group == null || group.getInterests() == null) continue;
                    for (String groupInterest : group.getInterests()) {
                        if (userInterests.contains(groupInterest)) {
                            filteredGroups.add(group);
                            break;
                        }
                    }
                }
                groupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup(GroupModel group, int position) {
        String groupId = group.getId();
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        // Add user to group's members
//
        //rootRef.child("groups").child(groupId).child("members").child(currentUserId).setValue("true");
//        groupsRef.child(group.getId()).child("members").setValue(uid);
        // Add group to user's joinedGroups
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupsRef.child(group.getId()).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> membersList = new ArrayList<>();
                for (DataSnapshot memberSnap : snapshot.getChildren()) {
                    String memberUid = memberSnap.getValue(String.class);
                    if (memberUid != null) membersList.add(memberUid);
                }
                if (!membersList.contains(uid)) {
                    membersList.add(uid);
                    groupsRef.child(group.getId()).child("members").setValue(membersList);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });



        rootRef.child("users").child(currentUserId).child("joinedGroups").child(groupId).setValue("true")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        joinedGroupIds.add(groupId);
                        groupsAdapter.notifyItemChanged(position);
                        Toast.makeText(getContext(), "Joined " + group.getName(), Toast.LENGTH_SHORT).show();
                        openGroupChat(group);
                    } else {
                        Toast.makeText(getContext(), "Failed to join group", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openGroupChat(GroupModel group) {
        android.content.Context context = getContext();
        android.content.Intent intent = new android.content.Intent(context, com.example.kiit_chatapp.activities.GroupChatActivity.class);
        intent.putExtra("groupId", group.getId());
        startActivity(intent);
    }

    // GroupModel class (add topic field)
    public static class GroupModel {
        private String id;
        private String name;
        private String topic; // Add this for group subject/topic
        private List<String> interests;

        public GroupModel() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }

        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
    }

    // Updated GroupsAdapter for custom card layout
    public static class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
        private List<GroupModel> groups;
        private Set<String> joinedGroupIds;
        private OnActionClickListener actionClickListener;

        public interface OnActionClickListener {
            void onActionClick(GroupModel group, int position, boolean alreadyJoined);
        }

        public GroupsAdapter(List<GroupModel> groups, Set<String> joinedGroupIds, OnActionClickListener listener) {
            this.groups = groups;
            this.joinedGroupIds = joinedGroupIds;
            this.actionClickListener = listener;
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            GroupModel group = groups.get(position);
            holder.txtGroupName.setText(group.getName());
            holder.groupTopicText.setText(group.getTopic() != null ? group.getTopic() : "No topic");

            boolean isJoined = joinedGroupIds.contains(group.getId());
            holder.joinButton.setText(isJoined ? "View Group" : "Join");
            holder.joinButton.setEnabled(true); // Always enabled

            holder.joinButton.setOnClickListener(v -> {
                if (actionClickListener != null) {
                    actionClickListener.onActionClick(group, position, isJoined);
                }
            });
        }

        @Override
        public int getItemCount() { return groups.size(); }

        static class GroupViewHolder extends RecyclerView.ViewHolder {
            TextView txtGroupName, groupTopicText;
            Button joinButton;

            public GroupViewHolder(@NonNull View itemView) {
                super(itemView);
                txtGroupName = itemView.findViewById(R.id.txtGroupName);
                groupTopicText = itemView.findViewById(R.id.groupTopicText);
                joinButton = itemView.findViewById(R.id.joinButton);
            }
        }
    }
}