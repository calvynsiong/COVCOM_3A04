package com.example.covcom.Controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.covcom.Constants;
import com.example.covcom.Entity.User;
import com.example.covcom.R;
import com.example.covcom.databinding.ActivityChatBinding;

public class ChatController extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiver;

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
        loadConversation();
    }

    private void loadConversation(){
        receiver = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        Log.d("FCM-f",receiver.name);
        String receiverName = receiver.name;
        binding.receiverTextName.setText(receiverName);
    }

    private void setListeners(){
        binding.backButton.setOnClickListener(v->returnToUserList());
    }

    private void returnToUserList(){
        startActivity(new Intent(getApplicationContext(), UserController.class));
    }
}