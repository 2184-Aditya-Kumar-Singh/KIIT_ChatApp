package com.example.kiit_chatapp.adapters;

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

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {

    private List<User> memberList;
    private OnKickClickListener kickClickListener;

    public interface OnKickClickListener {
        void onKick(User user);
    }

    public GroupMemberAdapter(List<User> memberList, OnKickClickListener kickClickListener) {
        this.memberList = memberList;
        this.kickClickListener = kickClickListener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = memberList.get(position);
        holder.tvName.setText(user.getName() != null ? user.getName() : "");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        holder.btnKick.setOnClickListener(v -> {
            if (kickClickListener != null) kickClickListener.onKick(user);
        });
    }

    @Override
    public int getItemCount() {
        return memberList != null ? memberList.size() : 0;
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        Button btnKick;
        MemberViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvMemberName);
            tvEmail = v.findViewById(R.id.tvMemberEmail);
            btnKick = v.findViewById(R.id.btnKick);
        }
    }
}