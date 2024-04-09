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
import com.example.covcom.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;

public class SignInController extends AppCompatActivity {

    private ActivitySignInBinding binding;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();

        preferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);


    }


    private Boolean isValidLogin() {
        if (binding.inputEmail.getText().toString().trim().isEmpty() || binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Invalid Credentials. Please enter proper username/email and password");
            startActivity(new Intent(getApplicationContext(), SignInController.class));
        }

        return true;
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void signIn() {
        HashMap<String, Object> user = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = binding.inputEmail.getText().toString();
        String password = binding.inputPassword.getText().toString();

        if (!isValidLogin()) showToast("Enter proper username/email and password");

        //Fake Password Authentication
        /*
        db.collection(Constants.DATABASE_USERS)
                .whereEqualTo(Constants.DATABASE_USERNAME, email)
                .whereEqualTo(Constants.DATABASE_FAKE, password)
                .get()
                .addOnCompleteListener(task2 -> {
                    List<DocumentSnapshot> documentResult2  = task2.getResult().getDocuments();
                    if (task2.isSuccessful() && task2.getResult() != null && documentResult2!=null) {
                        Log.d("FCM", "Added Fake user");
                        showToast("Logged in to Fake Account");
                        preferences.edit().putString(Constants.DATABASE_USERNAME, email).apply();
                        preferences.edit().putString(Constants.DATABASE_PASSWORD, password).apply();
                        preferences.edit().putString(Constants.KEY_USER_ID, documentResult2.get(0).getId()).apply();
                        showToast("Location has been shared, HR has been notified");
                        startActivity(new Intent(getApplicationContext(), UserController.class));
                    }


                })
                .addOnFailureListener(exception -> {
                    Log.d("FCM-f", exception.toString());
                    showToast("Failed to fetch data. Error 404");
                });
        */
        //End of Fake Password

        db.collection(Constants.DATABASE_USERS)
                .whereEqualTo(Constants.DATABASE_USERNAME, email)
                .whereEqualTo(Constants.DATABASE_PASSWORD, password)
                .get()
                .addOnCompleteListener(task -> {
                    List<DocumentSnapshot> documentResult  = task.getResult().getDocuments();
                    if (task.isSuccessful() && task.getResult() != null && documentResult!=null) {
                        Log.d("FCM", "Added user");
                        showToast("Logged in");

                        preferences.edit().putString(Constants.DATABASE_USERNAME, email).apply();
                        preferences.edit().putString(Constants.DATABASE_PASSWORD, password).apply();
                        preferences.edit().putString(Constants.KEY_USER_ID, documentResult.get(0).getId()).apply();

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
                    Log.d("FCM-f", "Registered");
                    signIn();
                }
        );

    }

}