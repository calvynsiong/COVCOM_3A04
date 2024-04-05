package com.example.covcom.Views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.covcom.Constants;
import com.example.covcom.databinding.ActivitySignInBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();




    };


    private Boolean isValidLogin(){
        if (binding.inputEmail.getText().toString().trim().isEmpty() || binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Invalid Credentials. Please enter proper username/email and password");
        }

        return true;
    }

    private void showToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void signIn(){
        HashMap<String,Object> user = new HashMap<>();
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        String  email =  binding.inputEmail.getText().toString();
        String password =  binding.inputPassword.getText().toString();

        if (!isValidLogin()) showToast("Enter proper username/email and password");

        db.collection(Constants.DATABASE_USERS)
        .whereEqualTo(Constants.DATABASE_USERNAME, email)
        .whereEqualTo(Constants.DATABASE_PASSWORD, password)
        .get()
        .addOnCompleteListener(task->{
            if (task.isSuccessful() && task.getResult()!=null && !task.getResult().getDocuments().isEmpty()){
                Log.d("FCM","Added user");
                showToast("Logged in");
                startActivity(new Intent(getApplicationContext(), ChatActivity.class));
            } else {
                showToast("Invalid Credentials");
            }

        }).addOnFailureListener(exception->{
            Log.d("FCM-f",exception.toString());
            showToast("Failed to fetch data. Error 404");
        });
    }

    private void setListeners(){
        binding.signInButton.setOnClickListener(v-> {
            Log.d("FCM-f","Registered");
            signIn();
                }
        );

    }

}