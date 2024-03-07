package com.example.superchat.Model;

public class UserSettings {
    private String emailAddress;
    private String about;
    private String profileImageUrl;

    public UserSettings() {
        // Default constructor required for Firebase
    }

    public UserSettings(String emailAddress, String about, String profileImageUrl) {
        this.emailAddress = emailAddress;
        this.about = about;
        this.profileImageUrl = profileImageUrl;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
