package com.example.covcom.Controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.covcom.Constants;

import javax.crypto.*;
//import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
public class KDCSController {
    private String user;
    private static final String algorithm = "AES";
    private FirebaseFirestore database;
    private static final String TAG = "KDCS";

    // Initialize KDCS for currently authenticated user
    public KDCSController(String user) {
        this.user = user;
        this.database = FirebaseFirestore.getInstance();
        // bonus: add a master key for encryption before storing on Firebase
    }

    // Create symmetric session keys for both chat users and store in Firestore
    // for specific chat session
    public void generateSessionKey(String recipient) throws Exception{
        SecretKey k =  KeyGenerator.getInstance(algorithm).generateKey();
        String keyStr = Base64.getEncoder().encodeToString(k.getEncoded());;
        addSessionKeyToDB(this.user, recipient, keyStr);
        addSessionKeyToDB(recipient, this.user, keyStr);
    }

    // Update Firestore session key for user
    private void addSessionKeyToDB(String sender, String recipient, String key) {
        String sessionKeyId = String.format("%s:%s", sender, recipient);
        this.database.collection(Constants.KDCS).document(sessionKeyId)
                .set(key)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Session key successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating session key", e);
                    }
                });
    }
}
