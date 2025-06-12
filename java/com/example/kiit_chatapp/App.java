package com.example.kiit_chatapp;

import android.app.Application;
import com.example.kiit_chatapp.utils.CloudinaryConfig;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CloudinaryConfig.init(this);

    }
}