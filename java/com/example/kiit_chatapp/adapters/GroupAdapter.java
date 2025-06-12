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

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Map<String, Object>> groupList;
    private List<Integer> tagCounts; // Each index matches groupList
    private OnGroupDeleteListener deleteListener;

    public interface OnGroupDeleteListener {
        void onDeleteGroupClicked(int position);
    }

    public GroupAdapter(List<Map<String, Object>> groupList, List<Integer> tagCounts, OnGroupDeleteListener listener) {
        this.groupList = groupList;
        this.tagCounts = tagCounts;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Map<String, Object> group = groupList.get(position);
        String name = (String) group.get("groupName");
        holder.txtGroupName.setText(name != null ? name : "Unnamed Group");
        holder.groupTopicText.setText("Do Not Use Any Vulgar Words, Contact 22052184@kiit.ac.in for support.");

        int count = tagCounts != null && tagCounts.size() > position ? tagCounts.get(position) : 0;
        if (count > 0) {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.tvUnreadBadge.setText(String.valueOf(count));
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
        }

        holder.btnDeleteGroup.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteGroupClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView txtGroupName;
        Button btnDeleteGroup, joinButton;
        TextView groupTopicText;
        TextView tvUnreadBadge;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            txtGroupName = itemView.findViewById(R.id.txtGroupName);
            btnDeleteGroup = itemView.findViewById(R.id.btnDeleteGroup);
            joinButton = itemView.findViewById(R.id.joinButton);
            groupTopicText = itemView.findViewById(R.id.groupTopicText);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
        }
    }
}