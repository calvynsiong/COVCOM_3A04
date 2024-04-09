package com.example.covcom.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.covcom.Adapters.UsersAdapter;
import com.example.covcom.Constants;
import com.example.covcom.Listeners.UserListener;
import com.example.covcom.Entity.User;
import com.example.covcom.R;
import com.example.covcom.databinding.ActivityUsersBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserController extends AppCompatActivity implements UserListener {

    private ActivityUsersBinding binding;
    private SharedPreferences sharedPreferences;
    private KDCSController kdcs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setListeners();
        getUsers();
        sharedPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);
        kdcs = new KDCSController(sharedPreferences);
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void returnToSignIn(){
        startActivity(new Intent(getApplicationContext(), SignInController.class));
    }

    private void returnToManageAccount(){
        startActivity(new Intent(getApplicationContext(), ManageAccountController.class));
    }

    private void setListeners(){
        binding.manageButton.setOnClickListener(v->returnToManageAccount());
        binding.backButton.setOnClickListener(t->{
          returnToSignIn();
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.DATABASE_USERS).get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = sharedPreferences.getString(Constants.KEY_USER_ID, "Default user");
                    if (!task.isSuccessful() || task.getResult() == null) {
                        showNoUsers();
                    }
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot entry : task.getResult()) {
                        String username = entry.getString(Constants.DATABASE_USERNAME);
                        String userId = entry.getId();
                        if (currentUserId.equals(userId)) continue;
                        User user = new User();
                        user.name = username;
                        user.id = userId;
                        users.add(user);
                        Log.d("FCM-f",username);

                    }
                    if (users.size() > 0) {
                        UsersAdapter usersAdapter = new UsersAdapter(users,this);
                        binding.usersRecyclerView.setAdapter(usersAdapter);
                        binding.usersRecyclerView.setVisibility(View.VISIBLE);

                    } else {
                        showNoUsers();
                    }

                });
    }

    private void showNoUsers() {
        binding.textErrorMessage.setText(String.format("No users available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {

        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatController.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        try {
            Log.d("KDCS", "Generate session key initiated");
            kdcs.generateSessionKey(user.name);

        } catch(Exception e) {
            Log.d("KDCS", e.toString());
        }
        finish();
    }
}