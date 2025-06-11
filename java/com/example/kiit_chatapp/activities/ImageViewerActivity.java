package com.example.kiit_chatapp.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kiit_chatapp.R;

public class ImageViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        ImageView imageView = findViewById(R.id.fullImageView);

        Glide.with(this).load(imageUrl).into(imageView);

        // Tap to dismiss
        imageView.setOnClickListener(v -> finish());
    }
}