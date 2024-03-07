package com.example.superchat.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.superchat.Model.users;
import com.example.superchat.R;

import java.util.List;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {

    private List<users> userList;
    private OnUserSelectedListener listener;


    public interface OnUserSelectedListener {
        void onUserSelected(users selectedUser);
    }

    public UserSelectionAdapter(List<users> userList, OnUserSelectedListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your user item layout
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        users user = userList.get(position);
        // Bind user data to the ViewHolder
        // For example: holder.userNameTextView.setText(user.getUserName());

        // Set click listener to handle user selection
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUserSelected(user);
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        // Your user item layout views (e.g., TextViews for user information)

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize your user item views
        }
    }
}
