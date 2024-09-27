package com.example.avaliapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapterReuni extends RecyclerView.Adapter<UserAdapterReuni.UserViewHolder> {

    private List<UserReuni> userList;
    private List<UserReuni> selectedUsers = new ArrayList<>(); // List to keep track of selected users

    public UserAdapterReuni(List<UserReuni> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserReuni user = userList.get(position);
        holder.textViewName.setText(user.getFullName());
        holder.checkBox.setChecked(selectedUsers.contains(user));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUsers.add(user);
            } else {
                selectedUsers.remove(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public List<UserReuni> getSelectedUsers() {
        return selectedUsers; // Return the list of selected users
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        CheckBox checkBox;

        public UserViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            checkBox = itemView.findViewById(R.id.checkBoxSelect);
        }
    }
}
