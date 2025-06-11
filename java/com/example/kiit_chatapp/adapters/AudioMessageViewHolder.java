package com.example.kiit_chatapp.adapters;

import android.media.MediaPlayer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;

public class AudioMessageViewHolder extends RecyclerView.ViewHolder {
    public ImageButton playPauseButton;
    public TextView audioLabel;

    public AudioMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        playPauseButton = itemView.findViewById(R.id.audioPlayPauseButton);
        audioLabel = itemView.findViewById(R.id.audioLabel);
    }
}