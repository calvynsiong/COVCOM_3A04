package com.example.covcom.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.covcom.Adapters.ChatAdapter;
import com.example.covcom.Constants;
import com.example.covcom.Controller.KDCSController;
import com.example.covcom.Entity.Message;
import com.example.covcom.Entity.User;
import com.example.covcom.R;
import com.example.covcom.databinding.ActivityChatBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ChatController extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiver;
    private List<Message> messages;
    private ChatAdapter chatAdapter;
    private SharedPreferences preferences;
    private FirebaseFirestore database;
    private EncryptionController encryptController;

    private void init(){
        preferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                messages,
                preferences.getString(Constants.KEY_USER_ID, "Default user")
                );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        try {
            String sessionKeyStr = preferences.getString(Constants.CHAT_SESSION_KEY, "default");
            SecretKey sessionKey = KDCSController.convertStringToKey(sessionKeyStr);
            encryptController = new EncryptionController(sessionKey);
        } catch (Exception e) {
            Log.d("KDCS", "Error in creating encryption agent:\t" + e);
        }
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();
        init();
        loadConversation();
        listenMessages();
        }

    private void loadConversation(){
        receiver = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        Log.d("FCM-f",receiver.name);
        String receiverName = receiver.name;
        binding.receiverTextName.setText(receiverName);
    }

    private void listenMessages(){
        database.collection(Constants.KEY_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferences.getString(Constants.KEY_USER_ID,""))
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiver.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiver.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferences.getString(Constants.KEY_USER_ID,""))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        String messageText;
        if (error!=null) return;
        if (value!=null){
            int count = messages.size();
            for (DocumentChange change: value.getDocumentChanges()){
                if (change.getType() == DocumentChange.Type.ADDED){
                    Message messageEntity = new Message();
                    QueryDocumentSnapshot document = change.getDocument();
                    messageEntity.senderId = document.getString(Constants.KEY_SENDER_ID);
                    messageEntity.receiverId= document.getString(Constants.KEY_RECEIVER_ID);
                    try {
                        messageText = encryptController.decrypt(document.getString(Constants.KEY_MESSAGE));
                    } catch (Exception e) {
                        Log.d("KDCS", "Error decrypting message" + e);
                        messageText = document.getString(Constants.KEY_MESSAGE);
                    }
                    messageEntity.message = messageText;
                    messageEntity.dateTime = getTimestamp(document.getDate(Constants.KEY_TIMESTAMP));
                    messageEntity.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                    messages.add(messageEntity);
                }
            }

            Collections.sort(messages, (m1,m2)-> {
                return m1.dateObject.compareTo(m2.dateObject);
            } );
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(messages.size(),messages.size());
                binding.chatRecyclerView.smoothScrollToPosition(messages.size()-1);

            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }

        binding.progressBar.setVisibility(View.GONE);


    };

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        // Currently using username for to id specific accounts/user for convenience, should switch
        Log.d("FCM-f",preferences.getString(Constants.KEY_USER_ID + "Is the sender", "Default user"));
        message.put(Constants.KEY_SENDER_ID,preferences.getString(Constants.KEY_USER_ID,""));
        message.put(Constants.KEY_RECEIVER_ID, receiver.id);
        try {
            String encryptedText = encryptController.encrypt(binding.inputMessage.getText().toString());
            message.put(Constants.KEY_MESSAGE,encryptedText);
        } catch (Exception e) {
            Log.d("KDCS", "Error encrypting message" + e);
            message.put(Constants.KEY_MESSAGE, "Error");
        }
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_CHAT).add(message);
        binding.inputMessage.setText(null);
    }

    private void setListeners(){
        binding.backButton.setOnClickListener(v->returnToUserList());
        binding.sendButton.setOnClickListener(v->sendMessage());
    }

    private void returnToUserList(){
        startActivity(new Intent(getApplicationContext(), UserController.class));
    }

    private String getTimestamp(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm", Locale.getDefault()).format(date);
    }

}