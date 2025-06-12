package com.example.kiit_chatapp.adapters;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnAudioClickListener {
        void onAudioClick(String audioUrl);
    }

    private static final int VIEW_TYPE_SENT_TEXT = 1;
    private static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    private static final int VIEW_TYPE_SENT_AUDIO = 3;
    private static final int VIEW_TYPE_RECEIVED_AUDIO = 4;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;
    private String currentUserEmail;
    private OnAudioClickListener audioClickListener;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId, String currentUserEmail, OnAudioClickListener audioClickListener) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.currentUserEmail = currentUserEmail;
        this.audioClickListener = audioClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if ("audio".equals(message.getType())) {
            if (message.getSenderId().equals(currentUserId)) {
                return VIEW_TYPE_SENT_AUDIO;
            } else {
                return VIEW_TYPE_RECEIVED_AUDIO;
            }
        } else {
            if (message.getSenderId().equals(currentUserId)) {
                return VIEW_TYPE_SENT_TEXT;
            } else {
                return VIEW_TYPE_RECEIVED_TEXT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT_AUDIO) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_audio_message_sent, parent, false);
            return new AudioMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_RECEIVED_AUDIO) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_audio_message_recieved, parent, false);
            return new AudioMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT_TEXT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (holder instanceof AudioMessageViewHolder) {
            AudioMessageViewHolder audioHolder = (AudioMessageViewHolder) holder;
            audioHolder.audioLabel.setText("[Recorded Audio]");

            audioHolder.playPauseButton.setOnClickListener(view -> {
                if (audioClickListener != null) {
                    audioClickListener.onAudioClick(message.getImageUrl());
                }
            });

        } else if (holder instanceof SentMessageViewHolder) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.sentMessageTextView.setText(message.getText());
            sentHolder.sentTimestampTextView.setText(message.getTimeStamp());

            // Make links clickable
            Linkify.addLinks(sentHolder.sentMessageTextView, Linkify.WEB_URLS);
            sentHolder.sentMessageTextView.setLinksClickable(true);
            sentHolder.sentMessageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.senderNameTextView.setText(message.getSenderName());
            receivedHolder.receivedMessageTextView.setText(message.getText());
            receivedHolder.receivedTimestampTextView.setText(message.getTimeStamp());

            // Make links clickable
            Linkify.addLinks(receivedHolder.receivedMessageTextView, Linkify.WEB_URLS);
            receivedHolder.receivedMessageTextView.setLinksClickable(true);
            receivedHolder.receivedMessageTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // --- ViewHolder classes ---

    public static class AudioMessageViewHolder extends RecyclerView.ViewHolder {
        ImageButton playPauseButton;
        TextView audioLabel;

        public AudioMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            playPauseButton = itemView.findViewById(R.id.audioPlayPauseButton);
            audioLabel = itemView.findViewById(R.id.audioLabel);
        }
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView sentMessageTextView, sentTimestampTextView;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessageTextView = itemView.findViewById(R.id.sentMessageTextView);
            sentTimestampTextView = itemView.findViewById(R.id.sentTimestampTextView);
        }
    }

    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView, receivedMessageTextView, receivedTimestampTextView;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
            receivedMessageTextView = itemView.findViewById(R.id.receivedMessageTextView);
            receivedTimestampTextView = itemView.findViewById(R.id.receivedTimestampTextView);
        }
    }
}