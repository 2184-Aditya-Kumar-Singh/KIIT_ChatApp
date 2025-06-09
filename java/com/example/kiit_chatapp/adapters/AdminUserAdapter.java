package com.example.kiit_chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.models.User;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserBanListener banListener;
    private OnUserBanListener unbanListener;

    public interface OnUserBanListener {
        void onUserBan(User user);
    }

    public AdminUserAdapter(Context context, List<User> userList, OnUserBanListener banListener, OnUserBanListener unbanListener) {
        this.context = context;
        this.userList = userList;
        this.banListener = banListener;
        this.unbanListener = unbanListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameTextView.setText(user.getName());
        holder.userEmailTextView.setText(user.getEmail());

        if (user.isBanned()) {
            holder.statusTextView.setText("BANNED");
            holder.btnBan.setVisibility(View.GONE);
            holder.btnUnban.setVisibility(View.VISIBLE);
        } else {
            holder.statusTextView.setText("ACTIVE");
            holder.btnBan.setVisibility(View.VISIBLE);
            holder.btnUnban.setVisibility(View.GONE);
        }

        holder.btnBan.setOnClickListener(v -> banListener.onUserBan(user));
        holder.btnUnban.setOnClickListener(v -> unbanListener.onUserBan(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, userEmailTextView, statusTextView;
        Button btnBan, btnUnban;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.tvUserName);
            userEmailTextView = itemView.findViewById(R.id.tvUserEmail);
            statusTextView = itemView.findViewById(R.id.tvUserStatus);
            btnBan = itemView.findViewById(R.id.btnBan);
            btnUnban = itemView.findViewById(R.id.btnUnban);
        }
    }
}