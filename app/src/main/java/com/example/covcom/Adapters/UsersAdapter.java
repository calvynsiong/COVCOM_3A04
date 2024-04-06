package com.example.covcom.Adapters;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covcom.Models.User;
import com.example.covcom.databinding.ItemContainerUserBinding;

import java.util.List;

public class UsersAdapter extends  RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private final List<User> users;

    public UsersAdapter (List<User> users){
        this.users = users;

    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainers = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        itemContainers.textName.setVisibility(View.VISIBLE);
        return new UserViewHolder(itemContainers);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.name);

        }
    }


}
