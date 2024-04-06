package com.example.covcom.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covcom.Entity.Message;
import com.example.covcom.databinding.ItemContainerReceivedMessageBinding;
import com.example.covcom.databinding.ItemContainerSentMessageBinding;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> conversation;
    private final String senderId;
    public static final int VIEW_SENT = 1;
    public static final int VIEW_RECEIVED = 2;

    public ChatAdapter(List<Message> conversation, String senderId) {
        this.senderId = senderId;
        this.conversation = conversation;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == VIEW_SENT ? new SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false)
                 ) :
                new ReceivedMessageViewHolder(
                        ItemContainerReceivedMessageBinding.inflate(
                                LayoutInflater.from(parent.getContext()),
                                parent,
                                false)
                );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position)==VIEW_SENT){
            ((SentMessageViewHolder) holder).setData(conversation.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(conversation.get(position));

        }
    }

    @Override
    public int getItemCount() {
        return conversation.size();
    }

    @Override
    public int getItemViewType(int position) {
        return conversation.get(position).senderId.equals(senderId) ? VIEW_SENT : VIEW_RECEIVED;
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }


        void setData(Message message) {
            binding.textMessage.setText(message.message);
            binding.textDateTime.setText(message.dateTime);
        }


    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }


        void setData(Message message) {
            binding.textMessage.setText(message.message);
            binding.textDateTime.setText(message.dateTime);
        }
    }


}
