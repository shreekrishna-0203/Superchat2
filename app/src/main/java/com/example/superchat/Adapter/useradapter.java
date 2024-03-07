package com.example.superchat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.superchat.Activity.Home;
import com.example.superchat.Activity.SelectOptionsDialog;
import com.example.superchat.Activity.chat;
import com.example.superchat.Model.users;
import com.example.superchat.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class useradapter extends RecyclerView.Adapter<useradapter.Viewholder> {
    Context home;
    ArrayList<users> usersArrayList;

    public useradapter(Home home, ArrayList<users> usersArrayList) {

        this.home = home;
        this.usersArrayList = usersArrayList;

    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(home).inflate(R.layout.item_user_row, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        users user = usersArrayList.get(position);

        // Set user data to views
        holder.user_name.setText(user.getName());
        holder.user_status.setText(user.getStatus());

        // Check if user_profile is not null before loading the image
        if (holder.user_profile != null) {
            Picasso.get().load(user.getImageURI()).into(holder.user_profile);
        }

        // Set background based on selection state
        int backgroundColor = user.isSelected() ? R.drawable.gradient : android.R.color.transparent;
        holder.itemView.setBackgroundResource(backgroundColor);

        // Handle click events
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle regular click (open chat, etc.)
                Intent intent = new Intent(home, chat.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("receiver-image", user.getImageURI());
                intent.putExtra("uid", user.getUid());
                home.startActivity(intent);
            }
        });

        // Handle long-press events
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Toggle selection state
                user.setSelected(!user.isSelected());

                // Notify adapter to update the UI
                notifyDataSetChanged();

                // Show the options dialog
                SelectOptionsDialog dialog = new SelectOptionsDialog();
                dialog.setListener(new SelectOptionsDialog.SelectOptionsDialogListener() {
                    @Override
                    public void onFavoriteSelected() {
                        // Handle favorite action
                    }

                    @Override
                    public void onDeleteSelected() {
                        // Handle delete action
                    }
                });
                dialog.show(((AppCompatActivity) home).getSupportFragmentManager(), "SelectOptionsDialog");

                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        ImageView user_profile;
        TextView user_name, user_status;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            user_profile = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            user_status = itemView.findViewById(R.id.user_status);
        }
    }
}



