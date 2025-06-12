package com.example.kiit_chatapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.adapters.MessageAdapter;
import com.example.kiit_chatapp.adapters.UserListAdapter;
import com.example.kiit_chatapp.models.Message;
import com.example.kiit_chatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.*;

public class GroupChatActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int PICK_AUDIO_REQUEST = 1002;
    private static final int REQUEST_PERMISSION_CODE = 42;

    private RecyclerView groupChatRecyclerView;
    private RecyclerView membersRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton, sendImageButton, recordAudioButton;
    private TextView groupNameTextView;
    private ProgressBar imageUploadProgressBar;
    private Button btnMembers;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private UserListAdapter userListAdapter;
    private List<User> memberList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference groupMessagesRef;
    private DatabaseReference usersRef;
    private DatabaseReference groupsRef;

    private String currentUserId;
    private String groupId = "default_group_id";
    private String currentUserName;
    private String currentUserEmail;
    private String currentUserRole;

    private ProgressDialog progressDialog;

    // Audio recording and preview
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    // Audio preview UI and playback
    private LinearLayout audioPreviewBar;
    private ImageButton btnPlayPause, btnSendAudio, btnCancelAudio;
    private TextView audioPreviewText;
    private MediaPlayer mediaPlayer;
    private boolean isAudioPlaying = false;
    private String pendingAudioFilePath = null;

    private boolean showingMembers = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        groupChatRecyclerView = findViewById(R.id.chatRecyclerView);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        sendImageButton = findViewById(R.id.sendImageButton);
        recordAudioButton = findViewById(R.id.recordAudioButton);
        groupNameTextView = findViewById(R.id.groupNameTextView);
        imageUploadProgressBar = findViewById(R.id.imageUploadProgressBar);
        btnMembers = findViewById(R.id.btnMembers);

        // Audio preview widgets
        audioPreviewBar = findViewById(R.id.audioPreviewBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnSendAudio = findViewById(R.id.btnSendAudio);
        btnCancelAudio = findViewById(R.id.btnCancelAudio);
        audioPreviewText = findViewById(R.id.audioPreviewText);

        if (audioPreviewBar != null) audioPreviewBar.setVisibility(View.GONE);

        if (btnPlayPause != null) btnPlayPause.setOnClickListener(v -> playPauseAudio());
        if (btnSendAudio != null) btnSendAudio.setOnClickListener(v -> sendPendingAudio());
        if (btnCancelAudio != null) btnCancelAudio.setOnClickListener(v -> cancelPendingAudio());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserEmail = mAuth.getCurrentUser().getEmail();
        if (currentUserEmail != null) currentUserEmail = currentUserEmail.toLowerCase();

        groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) groupId = "default_group_id";

        groupMessagesRef = FirebaseDatabase.getInstance().getReference("groups").child(groupId).child("messages");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        groupsRef.child(groupId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String groupName = snapshot.getValue(String.class);
                if (groupName != null) {
                    groupNameTextView.setText(groupName);
                } else {
                    groupNameTextView.setText("Group");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUserName = snapshot.child("name").getValue(String.class);
                currentUserEmail = snapshot.child("email").getValue(String.class);
                currentUserRole = snapshot.child("role").getValue(String.class);
                if (currentUserName == null) currentUserName = "Unknown";
                if (currentUserEmail == null) currentUserEmail = "";
                if (currentUserRole == null) currentUserRole = "";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId, currentUserEmail, audioUrl -> playAudio(audioUrl));
        groupChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupChatRecyclerView.setAdapter(messageAdapter);

        // Members RecyclerView setup
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListAdapter = new UserListAdapter(memberList);
        membersRecyclerView.setAdapter(userListAdapter);
        membersRecyclerView.setVisibility(View.GONE);

        loadMessages();
        sendButton.setOnClickListener(v -> sendMessage());
        sendImageButton.setOnClickListener(v -> checkAndPickImage());

        recordAudioButton.setOnClickListener(v -> {
            if (!isRecording && pendingAudioFilePath == null) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
                } else {
                    startRecording();
                    recordAudioButton.setImageResource(R.drawable.ic_stop);
                }
            } else if (isRecording) {
                stopRecordingAndShowPreview();
                recordAudioButton.setImageResource(R.drawable.mic_24);
            }
        });

        btnMembers.setOnClickListener(v -> {
            if (!showingMembers) {
                showMembersList();
            } else {
                showChatView();
            }
        });
    }

    private void showMembersList() {
        showingMembers = true;
        btnMembers.setText("See Chat");
        groupChatRecyclerView.setVisibility(View.GONE);
        if (audioPreviewBar != null) audioPreviewBar.setVisibility(View.GONE);
        membersRecyclerView.setVisibility(View.VISIBLE);
        loadGroupMembers();
    }

    private void showChatView() {
        showingMembers = false;
        btnMembers.setText("Members");
        groupChatRecyclerView.setVisibility(View.VISIBLE);
        if (audioPreviewBar != null) audioPreviewBar.setVisibility(View.GONE);
        membersRecyclerView.setVisibility(View.GONE);
    }

    private void loadGroupMembers() {
        memberList.clear();
        userListAdapter.notifyDataSetChanged();
        groupsRef.child(groupId).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot membersSnapshot) {
                List<String> memberUids = new ArrayList<>();
                for (DataSnapshot memberSnap : membersSnapshot.getChildren()) {
                    String uid = memberSnap.getValue(String.class);
                    if (uid != null) memberUids.add(uid);
                }
                if (memberUids.isEmpty()) {
                    userListAdapter.notifyDataSetChanged();
                    return;
                }
                for (String uid : memberUids) {
                    usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userSnap) {
                            String name = userSnap.child("name").getValue(String.class);
                            String email = userSnap.child("email").getValue(String.class);
                            User user = new User();
                            user.setUid(uid);
                            user.setName(name);
                            user.setEmail(email);
                            memberList.add(user);
                            userListAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void playAudio(String audioUrl) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            Toast.makeText(this, "Audio playback failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMessages() {
        groupMessagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    groupChatRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            }
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
            return;
        }
        String messageId = groupMessagesRef.push().getKey();
        if (messageId == null) return;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        List<String> mentionedEmails = extractMentionedEmails(messageText);

        List<String> hashNames = extractHashedNames(messageText);
        for (String hashName : hashNames) {
            String possibleEmail = hashName + "@kiit.ac.in";
            if (!mentionedEmails.contains(possibleEmail)) {
                mentionedEmails.add(possibleEmail);
            }
        }

        Message message = new Message(
                messageId,
                currentUserId,
                currentUserName,
                currentUserEmail,
                currentUserRole,
                messageText,
                "text",
                timeStamp,
                null,
                mentionedEmails
        );
        message.setTagSeenBy(new ArrayList<>());
        groupMessagesRef.child(messageId).setValue(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageEditText.setText("");
                    } else {
                        Toast.makeText(GroupChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private List<String> extractMentionedEmails(String text) {
        List<String> emails = new ArrayList<>();
        Pattern mentionPattern = Pattern.compile("#([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})");
        Matcher matcher = mentionPattern.matcher(text);
        while (matcher.find()) {
            String email = matcher.group(1).toLowerCase();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }
        return emails;
    }

    private List<String> extractHashedNames(String text) {
        List<String> names = new ArrayList<>();
        Pattern pattern = Pattern.compile("#([a-zA-Z0-9._-]+)\\b");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String tag = matcher.group(1).toLowerCase();
            if (!tag.contains("@")) {
                names.add(tag);
            }
        }
        return names;
    }

    private void checkAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_PERMISSION_CODE);
            } else {
                pickImage();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
            } else {
                pickImage();
            }
        }
    }

    private void checkAndPickAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        } else {
            pickAudio();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
            recordAudioButton.setImageResource(R.drawable.ic_stop);
        } else {
            Toast.makeText(this, "Permission denied to record audio.", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void pickAudio() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            if (requestCode == PICK_IMAGE_REQUEST) {
                uploadImageToCloudinary(fileUri);
            } else if (requestCode == PICK_AUDIO_REQUEST) {
                uploadAudioToCloudinary(fileUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        imageUploadProgressBar.setVisibility(View.VISIBLE);

        MediaManager.get().upload(imageUri)
                .unsigned("unsigned_chat_uploads")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        imageUploadProgressBar.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        imageUploadProgressBar.setVisibility(View.GONE);
                        String imageUrl = (String) resultData.get("secure_url");
                        sendImageMessage(imageUrl);
                    }
                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        imageUploadProgressBar.setVisibility(View.GONE);
                        Toast.makeText(GroupChatActivity.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        imageUploadProgressBar.setVisibility(View.GONE);
                    }
                })
                .dispatch();
    }

    private void uploadAudioToCloudinary(Uri audioUri) {
        imageUploadProgressBar.setVisibility(View.VISIBLE);

        MediaManager.get().upload(audioUri)
                .option("resource_type", "auto")
                .unsigned("unsigned_chat_uploads")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        imageUploadProgressBar.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        imageUploadProgressBar.setVisibility(View.GONE);
                        String audioUrl = (String) resultData.get("secure_url");
                        sendAudioMessage(audioUrl);
                    }
                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        imageUploadProgressBar.setVisibility(View.GONE);
                        Toast.makeText(GroupChatActivity.this, "Audio upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        imageUploadProgressBar.setVisibility(View.GONE);
                    }
                })
                .dispatch();
    }

    private void sendImageMessage(String imageUrl) {
        String messageId = groupMessagesRef.push().getKey();
        if (messageId == null) return;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Message message = new Message(
                messageId,
                currentUserId,
                currentUserName,
                currentUserEmail,
                currentUserRole,
                null,
                "image",
                timeStamp,
                imageUrl,
                null
        );
        message.setTagSeenBy(new ArrayList<>());
        groupMessagesRef.child(messageId).setValue(message)
                .addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void sendAudioMessage(String audioUrl) {
        String messageId = groupMessagesRef.push().getKey();
        if (messageId == null) return;

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Message message = new Message(
                messageId,
                currentUserId,
                currentUserName,
                currentUserEmail,
                currentUserRole,
                null,
                "audio",
                timeStamp,
                audioUrl,
                null
        );
        message.setTagSeenBy(new ArrayList<>());
        groupMessagesRef.child(messageId).setValue(message)
                .addOnFailureListener(e -> Toast.makeText(GroupChatActivity.this, "DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // --- Audio Recording Logic ---

    private void startRecording() {
        try {
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/audio_" + System.currentTimeMillis() + ".m4a";
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecordingAndShowPreview() {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                File audioFile = new File(audioFilePath);
                if (!audioFile.exists() || audioFile.length() == 0) {
                    Toast.makeText(this, "Audio file not found or is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                pendingAudioFilePath = audioFilePath;
                if (audioPreviewBar != null) audioPreviewBar.setVisibility(View.VISIBLE);
                if (recordAudioButton != null) recordAudioButton.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to stop recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void playPauseAudio() {
        if (pendingAudioFilePath == null) return;
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(pendingAudioFilePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                isAudioPlaying = true;
                mediaPlayer.setOnCompletionListener(mp -> {
                    btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                    isAudioPlaying = false;
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
            }
        } else if (isAudioPlaying) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            isAudioPlaying = false;
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            isAudioPlaying = true;
        }
    }

    private void sendPendingAudio() {
        if (pendingAudioFilePath == null) return;
        uploadAudioToCloudinary(Uri.fromFile(new File(pendingAudioFilePath)));
        cleanupAudioPreview();
    }

    private void cancelPendingAudio() {
        cleanupAudioPreview();
    }

    private void cleanupAudioPreview() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        pendingAudioFilePath = null;
        if (audioPreviewBar != null) audioPreviewBar.setVisibility(View.GONE);
        if (recordAudioButton != null) recordAudioButton.setVisibility(View.VISIBLE);
        if (btnPlayPause != null) btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
        isAudioPlaying = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        markTaggedAsSeen();
    }

    private void markTaggedAsSeen() {
        groupMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    Message msg = msgSnap.getValue(Message.class);
                    if (msg != null && msg.getMentionedEmails() != null && currentUserEmail != null
                            && msg.getMentionedEmails().contains(currentUserEmail)) {
                        List<String> tagSeenBy = msg.getTagSeenBy();
                        if (tagSeenBy == null) tagSeenBy = new ArrayList<>();
                        if (!tagSeenBy.contains(currentUserEmail)) {
                            tagSeenBy.add(currentUserEmail);
                            msgSnap.getRef().child("tagSeenBy").setValue(tagSeenBy);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}