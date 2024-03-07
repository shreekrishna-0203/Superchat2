package com.example.superchat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.superchat.Model.users;
import com.example.superchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity {

    TextView btn_signing;
    CircleImageView profile_image;
    EditText registration_name, registration_password, registration_confirm, registration_email, registration_phone;
    TextView btn_signup;
    FirebaseAuth auth;
    Uri Imageuri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);

        btn_signing = findViewById(R.id.signin_btn);
        profile_image = findViewById(R.id.profile_image);
        registration_name = findViewById(R.id.registration_name);
        registration_email = findViewById(R.id.registration_email);
        registration_password = findViewById(R.id.registration_password);
        registration_confirm = findViewById(R.id.registration_confirm);
        btn_signup = findViewById(R.id.btn_signup);

        btn_signing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Registration.this, Login.class));
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();

                String name = registration_name.getText().toString();
                String email = registration_email.getText().toString();
                String password = registration_password.getText().toString();
                String confirm = registration_confirm.getText().toString();
                status = "Yo.";

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
                    progressDialog.dismiss();
                    Toast.makeText(Registration.this, "Enter the data properly", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    progressDialog.dismiss();
                    registration_email.setError("Enter valid email");
                    Toast.makeText(Registration.this, "Enter valid email", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirm)) {
                    progressDialog.dismiss();
                    Toast.makeText(Registration.this, "Password is not the same", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    progressDialog.dismiss();
                    Toast.makeText(Registration.this, "Password Length should be at least 6 characters", Toast.LENGTH_SHORT).show();
                } else {
                    // Save user data
                    saveUserData(name, email, null);
                }
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
            Imageuri = data.getData();
            profile_image.setImageURI(Imageuri);
        }
    }

    private void saveUserData(String name, String email, String imageURI) {
        String uid = auth.getUid();
        if (uid != null) {
            DatabaseReference reference = database.getReference().child("user").child(uid);
            users users = new users(uid, name, email, imageURI, status);
            reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        startActivity(new Intent(Registration.this, Home.class));
                    } else {
                        Toast.makeText(Registration.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(Registration.this, "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }


}
