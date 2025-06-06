package com.example.kiit_chatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;

import java.util.List;
import java.util.Map;

public class UserManageAdapter extends RecyclerView.Adapter<UserManageAdapter.UserViewHolder> {

    private final List<Map<String, Object>> userList;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onBanClick(int position);
        void onDeleteClick(int position);
    }

    public UserManageAdapter(List<Map<String, Object>> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Map<String, Object> user = userList.get(position);
        String name = (String) user.get("name");
        String email = (String) user.get("email");
        Boolean isBanned = (Boolean) user.get("banned");

        holder.txtName.setText(name != null ? name : "No Name");
        holder.txtEmail.setText(email);
        holder.btnBan.setText(isBanned != null && isBanned ? "Unban" : "Ban");

        holder.btnBan.setOnClickListener(v -> listener.onBanClick(position));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail;
        Button btnBan, btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.userName);
            txtEmail = itemView.findViewById(R.id.userEmail);
            btnBan = itemView.findViewById(R.id.btnBan);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
