package com.example.superchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.superchat.Adapter.useradapter;
import com.example.superchat.Model.users;
import com.example.superchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;

    FirebaseAuth auth;
    RecyclerView mainuserRecyclerview;
    useradapter adapter;
    FirebaseDatabase database;
    ArrayList<users> usersArrayList;
    ImageView img_logout, home_search;
    ImageView menuImageView;


    Animation animate_toolbar, animate_recyclerview;
    LinearLayout linearLayout_toolbar;


    SwitchCompat switchMode;
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private List<users> userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersArrayList = new ArrayList<>();



        switchMode = findViewById(R.id.switchMode);
        sharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE);
        nightMode = sharedPreferences.getBoolean("nightMode", false);

        if (nightMode) {
            switchMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", false);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("nightMode", true);
                }
                editor.apply();
            }
        });

        menuImageView = findViewById(R.id.menu);


        menuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }

            private void showDialog() {


                showMenuDialog();
            }
        });

        DatabaseReference reference = database.getReference().child("user");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    users users = dataSnapshot.getValue(users.class);
                    usersArrayList.add(users);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
            }
        });

        img_logout = findViewById(R.id.img_logout);
        home_search = findViewById(R.id.home_search);

        mainuserRecyclerview = findViewById(R.id.mainuserRecyclerview);
        mainuserRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new useradapter(Home.this, usersArrayList);
        mainuserRecyclerview.setAdapter(adapter);

        img_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
                Toast.makeText(Home.this, "Loading", Toast.LENGTH_SHORT).show();
            }
        });

        home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchDialog();
            }

            private void showSearchDialog() {

            }
        });

        // Check and request permissions on the first launch
        if (isFirstLaunch()) {
            requestPermissions();
        }

        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(Home.this, Registration.class));
            finish();

        }

    }


    private boolean isFirstLaunch() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            preferences.edit().putBoolean("isFirstLaunch", false).apply();
            return true;
        }

        return false;
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.VIBRATE);
        }


        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, permissionsArray, PERMISSION_REQUEST_CODE);
        }
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(Home.this, R.style.Dialog);
        dialog.setContentView(R.layout.dialog_layout);

        dialog.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Home.this, Login.class));
                finish();
            }
        });

        dialog.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showMenuDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Dialog);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_menu, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        // Set the dialog window attributes to make it appear at the top right
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        dialog.getWindow().setAttributes(layoutParams);

        // Set up click listeners for each menu option
        AlertDialog finalDialog = dialog;

        // Group option
        view.findViewById(R.id.group_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Group option clicked", Toast.LENGTH_SHORT).show();
                // Handle group functionality
                groupContent();
                finalDialog.dismiss();
            }

            private void groupContent() {
                // Handle group functionality
            }
        });

        // Star option
        view.findViewById(R.id.star_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Star menu clicked", Toast.LENGTH_SHORT).show();
                // Handle star functionality
                starContent();
                finalDialog.dismiss();
            }

            private void starContent() {
                // Handle star functionality
            }
        });

        // Share option
        view.findViewById(R.id.share_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Share menu clicked", Toast.LENGTH_SHORT).show();
                // Handle share functionality
                shareContent();
                finalDialog.dismiss();
            }

            private void shareContent() {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Sharing this content!");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        // Tickmark option
        view.findViewById(R.id.tickmark_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Select", Toast.LENGTH_SHORT).show();
                // Handle tickmark functionality
                tickmarkContent();
                finalDialog.dismiss();
            }

            private void tickmarkContent() {
                // Handle tickmark functionality
            }
        });

        // Refresh option
        view.findViewById(R.id.refresh_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Refreshing", Toast.LENGTH_SHORT).show();
                // Handle refresh functionality
                refreshContent();
                finalDialog.dismiss();
            }

            private void refreshContent() {
                // Handle refresh functionality
            }
        });


        // Settings option
        view.findViewById(R.id.settings_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Opening settings", Toast.LENGTH_SHORT).show();
                // Handle settings functionality
                openSettingsActivity();
                finalDialog.dismiss();
            }

            private void openSettingsActivity() {
                // Create an Intent to start the Settings activity
                Intent intent = new Intent(Home.this, Settings.class);
                startActivity(intent);
            }
        });


        // Exit option

        view.findViewById(R.id.exit_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home.this, "Closing the pop-up menu", Toast.LENGTH_SHORT).show();
                // Handle exit functionality

                finalDialog.dismiss();
            }

        });

        dialog = builder.create();
        dialog.show();
    }


}
