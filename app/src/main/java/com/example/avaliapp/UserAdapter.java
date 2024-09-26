package com.example.avaliapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private OnUserSelectedListener onUserSelectedListener;

    public interface OnUserSelectedListener {
        void onUserSelected(User user, boolean isSelected);
    }

    public UserAdapter(List<User> userList, OnUserSelectedListener onUserSelectedListener) {
        this.userList = userList;
        this.onUserSelectedListener = onUserSelectedListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.textViewName.setText(user.getFullName());
        holder.textViewCargo.setText(user.getArea());

        holder.checkBoxSelect.setOnCheckedChangeListener(null); // Remove o listener para evitar chamadas indesejadas
        holder.checkBoxSelect.setChecked(false);

        holder.checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            onUserSelectedListener.onUserSelected(user, isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void updateUserList(List<User> newUserList) {
        userList.clear();
        userList.addAll(newUserList);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewCargo;
        CheckBox checkBoxSelect;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCargo = itemView.findViewById(R.id.textViewCargo);
            checkBoxSelect = itemView.findViewById(R.id.checkBoxSelect);
        }
    }
}
