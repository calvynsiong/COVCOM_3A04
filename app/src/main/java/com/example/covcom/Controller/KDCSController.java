package com.example.covcom.Controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.covcom.Constants;
import com.google.firebase.firestore.SetOptions;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.HashMap;
public class KDCSController {
    private String user;
    private FirebaseFirestore database;
    private HashMap<String, SecretKey> sessionKeyMap;
    private static final String ALGO = "AES";
    private static final String TAG = "KDCS";

    interface OnSessionKeyUpdatedCallback {
        void onSessionKeyUpdated();
    }

    // Initialize KDCS for currently authenticated user
    public KDCSController(String user) {
        this.user = user;
        this.database = FirebaseFirestore.getInstance();
        this.sessionKeyMap = new HashMap<String, SecretKey>();
        // bonus: add a master key for encryption before storing on Firebase
    }

    // Create symmetric session keys for both chat users and store in Firestore
    // for specific chat session
    public void generateSessionKey(String recipient) throws Exception {
        // Create new session key if does not exist
        // Stretch goal: update session key after set period of time
        this.updateSessionKeyMap(recipient, () -> {
            if (this.sessionKeyMap.containsKey(recipient)) {
                Log.d(TAG, "Session key already exists:\t" + this.sessionKeyMap.get(recipient));
            }
            else {
                try {
                    SecretKey k =  KeyGenerator.getInstance(ALGO).generateKey();
                    String keyStr = Base64.getEncoder().encodeToString(k.getEncoded());;
                    addSessionKeyToDB(this.user, recipient, keyStr);
                    addSessionKeyToDB(recipient, this.user, keyStr);
                } catch (Exception e) {
                    Log.d(TAG, "Error generating session key" + e);
                }
            }
        });
    }

    public SecretKey getSessionKey(String recipient) {
        return this.sessionKeyMap.get(recipient);
    }

    private void updateSessionKeyMap(String recipient, OnSessionKeyUpdatedCallback listener) throws Exception {
        this.getSessionKeyFromDB(recipient)
                .addOnSuccessListener(keyStr -> {
                    if (keyStr != null) {
                        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
                        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGO);
                        this.sessionKeyMap.put(recipient, key);
                    }
                    listener.onSessionKeyUpdated();
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error fetching session key:\t" + e);
                });
    }

    // Update Firestore session key for user
    private void addSessionKeyToDB(String sender, String recipient, String key) {
        HashMap<String, String> recipientKeyPair = new HashMap<String, String>();
        recipientKeyPair.put(recipient, key);
        this.database.collection(Constants.DATABASE_KDCS).document(sender)
                .set(recipientKeyPair, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Session key updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating session key", e);
                    }
                });
    }

    private Task<String> getSessionKeyFromDB(String recipient) {
        return this.database.collection(Constants.DATABASE_KDCS).document(this.user)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        // Document found in the offline cache
                        DocumentSnapshot document = task.getResult();
                        Log.d(TAG, "Cached document data: " + document.getData());
                        if (document.exists()) {
                            return document.getString(recipient);
                        } else {
                            return null;
                        }

                    } else {
                        Log.d(TAG, "Cached get failed: ", task.getException());
                        throw(task.getException());
                    }
                });
    }
}
