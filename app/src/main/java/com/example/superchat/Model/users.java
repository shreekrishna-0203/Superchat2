package com.example.superchat.Model;

import com.google.firebase.messaging.FirebaseMessaging;

public class users {
    public String name;
    public String imageURI;
    public String status;
    private String uid;
    private String email;
    private boolean isSelected;
    private String fcmtoken;

    public users() {
    }

    public users(String uid, String name, String email, String imageURI, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.imageURI = imageURI;
        this.status = status;


        // Subscribe to the topic when a user is created
        subscribeToTopic();
    }

    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("user_" + uid);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


}
