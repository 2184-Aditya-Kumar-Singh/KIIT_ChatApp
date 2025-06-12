package com.example.kiit_chatapp.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static DatabaseReference getUserReference() {
        return FirebaseDatabase.getInstance().getReference(Constants.USERS);
    }

    public static DatabaseReference getGroupReference() {
        return FirebaseDatabase.getInstance().getReference(Constants.GROUPS);
    }

    public static DatabaseReference getMessageReference(String groupId) {
        return FirebaseDatabase.getInstance().getReference(Constants.MESSAGES).child(groupId);
    }

    public static DatabaseReference getChatRequestReference() {
        return FirebaseDatabase.getInstance().getReference(Constants.CHAT_REQUESTS);
    }
}
