package com.example.kiit_chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messages;
    private MediaPlayer mediaPlayer;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        this.mediaPlayer = new MediaPlayer();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.sentMessageTextView.setVisibility(View.GONE);
            sentHolder.sentTimestampTextView.setVisibility(View.GONE);

            // Show text
            if ("text".equals(msg.getType())) {
                sentHolder.sentMessageTextView.setVisibility(View.VISIBLE);
                sentHolder.sentMessageTextView.setText(msg.getText());
                sentHolder.sentMessageTextView.setOnClickListener(null);
            } else if ("image".equals(msg.getType())) {
                sentHolder.sentMessageTextView.setVisibility(View.VISIBLE);
                sentHolder.sentMessageTextView.setText("[Image]");
                sentHolder.sentMessageTextView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(msg.getFileUrl()));
                    context.startActivity(intent);
                });
            } else if ("audio".equals(msg.getType())) {
                sentHolder.sentMessageTextView.setVisibility(View.VISIBLE);
                sentHolder.sentMessageTextView.setText("[Audio] Tap to play");
                sentHolder.sentMessageTextView.setOnClickListener(v -> {
                    try {
                        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(msg.getFileUrl());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            sentHolder.sentTimestampTextView.setVisibility(View.VISIBLE);
            sentHolder.sentTimestampTextView.setText(msg.getTimeStamp());

        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder recvHolder = (ReceivedMessageViewHolder) holder;
            recvHolder.senderNameTextView.setVisibility(View.VISIBLE);
            recvHolder.receivedMessageTextView.setVisibility(View.GONE);
            recvHolder.receivedTimestampTextView.setVisibility(View.GONE);

            // Show sender name
            String senderName = msg.getSenderName();
            if (senderName == null || senderName.trim().isEmpty()) {
                senderName = "Unknown";
            }
            recvHolder.senderNameTextView.setText(senderName);

            // Show roll number if role=student & email endsWith @kiit.ac.in
            String senderRole = msg.getSenderRole();
            String senderEmail = msg.getSenderEmail();
            if ("student".equalsIgnoreCase(senderRole)
                    && senderEmail != null && senderEmail.endsWith("@kiit.ac.in")) {
                String roll = senderEmail.substring(0, senderEmail.indexOf("@"));
                recvHolder.rollNumberTextView.setVisibility(View.VISIBLE);
                recvHolder.rollNumberTextView.setText("Roll: " + roll);
            } else {
                recvHolder.rollNumberTextView.setVisibility(View.GONE);
            }

            // Show text
            if ("text".equals(msg.getType())) {
                recvHolder.receivedMessageTextView.setVisibility(View.VISIBLE);
                recvHolder.receivedMessageTextView.setText(msg.getText());
                recvHolder.receivedMessageTextView.setOnClickListener(null);
            } else if ("image".equals(msg.getType())) {
                recvHolder.receivedMessageTextView.setVisibility(View.VISIBLE);
                recvHolder.receivedMessageTextView.setText("[Image]");
                recvHolder.receivedMessageTextView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(msg.getFileUrl()));
                    context.startActivity(intent);
                });
            } else if ("audio".equals(msg.getType())) {
                recvHolder.receivedMessageTextView.setVisibility(View.VISIBLE);
                recvHolder.receivedMessageTextView.setText("[Audio] Tap to play");
                recvHolder.receivedMessageTextView.setOnClickListener(v -> {
                    try {
                        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(msg.getFileUrl());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            recvHolder.receivedTimestampTextView.setVisibility(View.VISIBLE);
            recvHolder.receivedTimestampTextView.setText(msg.getTimeStamp());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView sentMessageTextView, sentTimestampTextView;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessageTextView = itemView.findViewById(R.id.sentMessageTextView);
            sentTimestampTextView = itemView.findViewById(R.id.sentTimestampTextView);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView, rollNumberTextView, receivedMessageTextView, receivedTimestampTextView;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            rollNumberTextView = itemView.findViewById(R.id.rollNumberTextView);
            receivedMessageTextView = itemView.findViewById(R.id.receivedMessageTextView);
            receivedTimestampTextView = itemView.findViewById(R.id.receivedTimestampTextView);
        }
    }
}