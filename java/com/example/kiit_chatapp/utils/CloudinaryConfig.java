package com.example.kiit_chatapp.utils;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized) return;
        try {
            Map config = new HashMap();
            config.put("cloud_name", "dgcahtdhy"); // <-- CHANGE THIS
            config.put("upload_preset", "unsigned_chat_uploads"); // <-- CHANGE THIS
            MediaManager.init(context, config);
            initialized = true;
        } catch (IllegalStateException e) {
            // Already initialized; ignore
            initialized = true;
        }
    }
}