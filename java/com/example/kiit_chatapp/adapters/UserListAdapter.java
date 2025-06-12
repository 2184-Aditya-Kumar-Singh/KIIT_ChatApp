package com.example.kiit_chatapp.adapters;
//for group user list in groups
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kiit_chatapp.R;
import com.example.kiit_chatapp.models.User;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<User> userList;

    public UserListAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userNameText.setText(user.getName() != null ? user.getName() : "");
        holder.userEmailTextView.setText(user.getEmail() != null ? user.getEmail() : "");
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText, userEmailTextView;

        UserViewHolder(View v) {
            super(v);
            userNameText = v.findViewById(R.id.userNameText);
            userEmailTextView = v.findViewById(R.id.userEmailTextView);
        }
    }
}
