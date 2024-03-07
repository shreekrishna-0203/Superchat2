package com.example.superchat.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.superchat.Model.UserSettings;
import com.example.superchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity {

    private EditText editTextEmailAddress;
    private EditText editTextAbout;
    private Button buttonSave;
    private ImageView profile_image;
    private static final String EMAIL_ADDRESS_KEY = "emailAddress";
    private static final String ABOUT_KEY = "about";
    private static final String PROFILE_IMAGE_URL_KEY = "profileImageUrl";

    private Uri imageUri;
    private CircleImageView circleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Initialize views
        editTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        editTextAbout = findViewById(R.id.editTextAbout);
        buttonSave = findViewById(R.id.button2);
        profile_image = findViewById(R.id.profile_image2);

        // Load saved settings
        loadSettings();

        // Set click listeners
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle save button click
                saveSettings();
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 5);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 5 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profile_image.setImageURI(imageUri);
        }
    }

    private void loadSettings() {
        // Load saved email address, "About" content, and profile image URL
        loadEmailAddress();
        loadAbout();
        loadProfileImageUrl();
    }

    private void loadEmailAddress() {
        SharedPreferences prefs = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        String savedEmailAddress = prefs.getString(EMAIL_ADDRESS_KEY, "");

        // Load the saved email address to the EditText
        editTextEmailAddress.setText(savedEmailAddress);
    }

    private void loadAbout() {
        SharedPreferences prefs = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        String savedAbout = prefs.getString(ABOUT_KEY, "");

        // Load the saved "About" content to the EditText
        editTextAbout.setText(savedAbout);
    }

    private void loadProfileImageUrl() {
        SharedPreferences prefs = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        String savedProfileImageUrl = prefs.getString(PROFILE_IMAGE_URL_KEY, "");

        // Load the saved profile image URL to the ImageView
        // You can use an image loading library like Picasso or Glide here
        // For simplicity, we'll just set the URL as a placeholder

    }

    private void saveSettings() {
        // Save the email address to SharedPreferences
        String emailAddress = editTextEmailAddress.getText().toString().trim();
        saveEmailAddress(emailAddress);

        // Save the "About" content to SharedPreferences
        String about = editTextAbout.getText().toString().trim();
        saveAbout(about);

        // Save the profile image URL to SharedPreferences and Firebase
        if (imageUri != null) {
            String profileImageUrl = imageUri.toString();
            saveProfileImageUrl(profileImageUrl);
            saveProfileImageUrlToFirebase(profileImageUrl);
        }

        // Show a "Saved" message
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    private void saveEmailAddress(String emailAddress) {
        SharedPreferences prefs = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL_ADDRESS_KEY, emailAddress);
        editor.apply();
    }

    private void saveAbout(String about) {
        SharedPreferences prefs = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ABOUT_KEY, about);
        editor.apply();
    }

    private void saveProfileImageUrl(String profileImageUrl) {
        SharedPreferences prefs = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid(), MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROFILE_IMAGE_URL_KEY, profileImageUrl);
        editor.apply();
    }

    private void saveProfileImageUrlToFirebase(String profileImageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid())
                    .child("profileImageUrl");
            databaseReference.setValue(profileImageUrl);
        }
    }
}
