package com.example.superchat.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.superchat.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    //Widgets
    Button btn;
    Animation animate_btn, animate_txt;
    TextView t1,t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        btn = findViewById(R.id.button);
        t1 = findViewById(R.id.textView);
        t2 = findViewById(R.id.textView2);


        //Animation

        animate_btn = AnimationUtils.loadAnimation(this,R.anim.animate_btn);
        animate_txt = AnimationUtils.loadAnimation(this,R.anim.animate_text);

        btn.setAnimation(animate_btn);

        //Animation for text
        t1.setAnimation(animate_txt);
        t2.setAnimation(animate_txt);



        //To go to login page
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Login.class));
            }
        });




    }

}
