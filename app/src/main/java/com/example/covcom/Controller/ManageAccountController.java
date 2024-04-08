package com.example.covcom.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covcom.Constants;
import com.example.covcom.databinding.ActivityManageBinding;
import com.google.firebase.firestore.FirebaseFirestore;

public class ManageAccountController extends AppCompatActivity {
    private SharedPreferences preferences;
    private FirebaseFirestore database;
    private ActivityManageBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);


    }
}
