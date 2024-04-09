package com.example.covcom.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covcom.Constants;
import com.example.covcom.Entity.User;
import com.example.covcom.databinding.ActivityManageBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ManageAccountController extends AppCompatActivity {
    private ActivityManageBinding binding;
    private SharedPreferences preferences;
    private FirebaseFirestore database;


    private void init() {
        preferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        String name = preferences.getString(Constants.DATABASE_USERNAME, "Default user");
        binding.textView8.setText(name);
        String email = preferences.getString(Constants.DATABASE_EMAIL, "No email");
        binding.textView9.setText(email);
        String phoneNum = preferences.getString(Constants.DATABASE_PHONENUM, "No Phone Number");
        binding.textView10.setText(phoneNum);
        String timezone = preferences.getString(Constants.DATABASE_TIMEZONE, "EST");
        binding.textView11.setText(timezone);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();

        preferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

        init();
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidPassword() {
        if (binding.inputCurrentPassword.getText().toString().trim().isEmpty()) {
            showToast("Invalid Credentials. Please enter current password");
        }

        return true;
    }

    private void updateAccount() {
        database = FirebaseFirestore.getInstance();
        String name = binding.inputName.getText().toString();
        String email = binding.inputEmail.getText().toString();
        String phoneNum =  binding.inputPhoneNum.getText().toString();
        String timezone =  binding.inputTimezone.getText().toString();
        String currentPassword = binding.inputCurrentPassword.getText().toString();
        String newPassword = binding.inputNewPassword.getText().toString();

        if (!isValidPassword()) {
            showToast("Enter current password to save changes");
            startActivity(new Intent(getApplicationContext(), ManageAccountController.class));
        };

        database.collection(Constants.DATABASE_USERS)
                .whereEqualTo(Constants.DATABASE_USERNAME, preferences.getString(Constants.DATABASE_USERNAME, "Default user"))
                .whereEqualTo(Constants.DATABASE_PASSWORD, currentPassword)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentResult  = task.getResult().getDocuments().get(0);
                    if (task.isSuccessful() && task.getResult() != null && documentResult!=null) {
                        Log.d("FCM", "Modified user");
                        showToast("Modified User");

                        if (!binding.inputName.getText().toString().trim().isEmpty()) {
                            Log.d("FCM", "Modified name");
                            preferences.edit().putString(Constants.DATABASE_USERNAME, name).apply();
                        }
                        if (!binding.inputEmail.getText().toString().trim().isEmpty()) {
                            Log.d("FCM", "Modified email");
                            preferences.edit().putString(Constants.DATABASE_EMAIL, email).apply();
                        }
                        if (!binding.inputPhoneNum.getText().toString().trim().isEmpty()) {
                            Log.d("FCM", "Modified phone");
                            preferences.edit().putString(Constants.DATABASE_PHONENUM, phoneNum).apply();
                        }
                        if (!binding.inputTimezone.getText().toString().trim().isEmpty()) {
                            Log.d("FCM", "Modified timezone");
                            preferences.edit().putString(Constants.DATABASE_TIMEZONE, timezone).apply();
                        }
                        if (!binding.inputNewPassword.getText().toString().trim().isEmpty()) {
                            Log.d("FCM", "Modified password");
                            preferences.edit().putString(Constants.DATABASE_PASSWORD, newPassword).apply();
                        }

                        startActivity(new Intent(getApplicationContext(), UserController.class));
                    } else {
                        showToast("Invalid Credentials");
                    }

                })
                .addOnFailureListener(exception -> {
                    Log.d("FCM-f", exception.toString());
                    showToast("Failed to fetch data. Error 404");
                });
    }

    private void setListeners() {
        binding.signInButton.setOnClickListener(v -> {
                    Log.d("FCM-f", "Updated");
                    updateAccount();
                }
        );
        binding.backButton.setOnClickListener(t->{
            startActivity(new Intent(getApplicationContext(), UserController.class));
        });

    }
}
