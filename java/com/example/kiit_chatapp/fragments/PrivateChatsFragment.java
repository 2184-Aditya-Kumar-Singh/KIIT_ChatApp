package com.example.kiit_chatapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.activities.PrivateChatActivity;
import com.example.kiit_chatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateChatsFragment extends Fragment {

    private EditText etSearchEmail;
    private Button btnSearch;
    private LinearLayout resultLayout;
    private TextView tvName, tvBranch, tvYear, tvInterests, tvEmail, tvRole;
    private Button btnStartChat;

    private DatabaseReference usersRef, chatListRef;
    private User foundUser;

    private RecyclerView recyclerChatList;
    private ChatListAdapter chatListAdapter;
    private List<User> chatUsers = new ArrayList<>();

    private String currentUserId;
    private Map<String, Long> unreadCounts = new HashMap<>();

    // Move the interface OUTSIDE the adapter class, but inside the fragment
    public interface OnUserClickListener {
        void onUserClick(String userId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_chats, container, false);

        etSearchEmail = view.findViewById(R.id.etSearchEmail);
        btnSearch = view.findViewById(R.id.btnSearch);
        resultLayout = view.findViewById(R.id.resultLayout);
        tvName = view.findViewById(R.id.tvName);
        tvBranch = view.findViewById(R.id.tvBranch);
        tvYear = view.findViewById(R.id.tvYear);
        tvInterests = view.findViewById(R.id.tvInterests);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvRole = view.findViewById(R.id.tvRole);
        btnStartChat = view.findViewById(R.id.btnStartChat);

        recyclerChatList = view.findViewById(R.id.recyclerChatList);
        recyclerChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatListAdapter = new ChatListAdapter(chatUsers, userId -> openChat(userId));
        recyclerChatList.setAdapter(chatListAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatListRef = FirebaseDatabase.getInstance().getReference("private_chats_list");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        resultLayout.setVisibility(View.GONE);

        btnSearch.setOnClickListener(v -> searchUserByEmail());

        btnStartChat.setOnClickListener(v -> {
            if (foundUser != null && foundUser.getUid() != null) {
                addToChatList(foundUser.getUid());
                openChat(foundUser.getUid());
            }
        });

        loadUserChats();

        return view;
    }

    private void searchUserByEmail() {
        String enteredEmail = etSearchEmail.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(enteredEmail) || !enteredEmail.endsWith("@kiit.ac.in")) {
            etSearchEmail.setError("Enter a valid KIIT email");
            return;
        }

        usersRef.orderByChild("email").equalTo(enteredEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            User user = safeGetUser(userSnap);
                            if (user != null) {
                                if (!user.getUid().equals(currentUserId)) {
                                    foundUser = user;
                                    showUserResult(user);
                                    found = true;
                                } else {
                                    resultLayout.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Cannot chat with yourself!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        if (!found) {
                            resultLayout.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "User not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Defensive user parsing for old, new, or malformed users
    private User safeGetUser(DataSnapshot userSnap) {
        User user = null;
        try {
            user = userSnap.getValue(User.class);
        } catch (DatabaseException e) {
            // try manual fallback for legacy/malformed data
            try {
                user = new User();
                user.setUid(userSnap.getKey());
                user.setName((String) userSnap.child("name").getValue(String.class));
                user.setBranch((String) userSnap.child("branch").getValue(String.class));
                user.setYear((String) userSnap.child("year").getValue(String.class));
                user.setEmail((String) userSnap.child("email").getValue(String.class));
                user.setRole((String) userSnap.child("role").getValue(String.class));
                // Defensive: interests can be List<String> or String or Boolean
                Object raw = userSnap.child("interests").getValue();
                if (raw instanceof List) {
                    user.setInterests((List<String>) raw);
                } else if (raw instanceof String) {
                    List<String> list = new ArrayList<>();
                    if (!TextUtils.isEmpty((String)raw)) list.add((String) raw);
                    user.setInterests(list);
                } else {
                    user.setInterests(new ArrayList<>());
                }
            } catch (Exception ignored) {}
        }
        if (user != null && user.getUid() == null) user.setUid(userSnap.getKey());
        return user;
    }

    private void showUserResult(User user) {
        resultLayout.setVisibility(View.VISIBLE);

        tvName.setText("Name: " + user.getName());
        tvBranch.setText("Branch: " + user.getBranch());
        tvYear.setText("Year: " + user.getYear());
        List<String> interests = user.getInterests();
        String interestsStr = (interests != null && !interests.isEmpty()) ? interests.toString() : "None";
        tvInterests.setText("Interests: " + interestsStr);
        tvEmail.setText("Email: " + user.getEmail());
        tvRole.setText("Role: " + user.getRole());
    }

    private void addToChatList(String otherUserId) {
        Map<String, Object> chatMeta = new HashMap<>();
        chatMeta.put("lastMessage", ""); // Can update with last message later
        chatMeta.put("timestamp", System.currentTimeMillis());

        chatListRef.child(currentUserId).child(otherUserId).updateChildren(chatMeta);
        chatListRef.child(otherUserId).child(currentUserId).updateChildren(chatMeta);
    }

    private void loadUserChats() {
        chatListRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatUsers.clear();
                unreadCounts.clear();
                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String otherUid = chatSnap.getKey();
                    long unread = 0;
                    if (chatSnap.child("unreadCount").exists()) {
                        Object val = chatSnap.child("unreadCount").getValue();
                        if (val instanceof Long) unread = (Long) val;
                        else if (val instanceof Integer) unread = ((Integer)val).longValue();
                    }
                    unreadCounts.put(otherUid, unread);

                    usersRef.child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                            User user = safeGetUser(userSnap);
                            if (user != null) {
                                boolean alreadyAdded = false;
                                for (User u : chatUsers) {
                                    if (u.getUid() != null && u.getUid().equals(user.getUid())) {
                                        alreadyAdded = true;
                                        break;
                                    }
                                }
                                if (!alreadyAdded) {
                                    chatUsers.add(user);
                                    chatListAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void openChat(String otherUserId) {
        Intent intent = new Intent(getContext(), PrivateChatActivity.class);
        intent.putExtra("otherUserId", otherUserId);
        startActivity(intent);
    }

    public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
        private final List<User> users;
        private final OnUserClickListener listener;

        public ChatListAdapter(List<User> users, OnUserClickListener listener) {
            this.users = users;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_private_chat, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            User user = users.get(position);
            holder.tvName.setText(user.getName());
            holder.tvEmail.setText(user.getEmail());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null && user.getUid() != null) {
                    listener.onUserClick(user.getUid());
                }
            });

            // Show unread badge if >0
            Long unread = unreadCounts.get(user.getUid());
            if (unread != null && unread > 0) {
                holder.tvUnreadBadge.setText(unread > 99 ? "99+" : unread.toString());
                holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            } else {
                holder.tvUnreadBadge.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvUnreadBadge;
            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvChatUserName);
                tvEmail = itemView.findViewById(R.id.tvChatUserEmail);
                tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            }
        }
    }
}