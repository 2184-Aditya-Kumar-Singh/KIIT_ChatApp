package com.example.kiit_chatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.models.InterestModel;

import java.util.ArrayList;
import java.util.List;

public class InterestAdapter extends RecyclerView.Adapter<InterestAdapter.InterestViewHolder> {
    private final List<InterestModel> interests;

    public InterestAdapter(List<InterestModel> interests) {
        this.interests = interests;
    }

    @NonNull
    @Override
    public InterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_interest_checkbox, parent, false);
        return new InterestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestViewHolder holder, int position) {
        InterestModel interest = interests.get(position);

        // Remove old listener to prevent recycled view issues
        holder.checkBox.setOnCheckedChangeListener(null);

        holder.checkBox.setText(interest.getName());
        holder.checkBox.setChecked(interest.isSelected());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            interest.setSelected(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return interests.size();
    }

    static class InterestViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        public InterestViewHolder(View v) {
            super(v);
            checkBox = v.findViewById(R.id.checkboxInterest);
        }
    }

    public List<InterestModel> getInterests() {
        return interests;
    }

    // Add this method to update the interests list and notify adapter
    public void setInterests(List<InterestModel> newInterests) {
        this.interests.clear();
        this.interests.addAll(newInterests);
        notifyDataSetChanged();
    }
}