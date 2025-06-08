package com.example.kiit_chatapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kiit_chatapp.models.User;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public void setUser(User user) {
        currentUser.setValue(user);
    }

    public LiveData<User> getUser() {
        return currentUser;
    }
}
