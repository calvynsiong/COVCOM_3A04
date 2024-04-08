package com.example.covcom.Controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    private SharedPreferences sharedPreferences;
    private static final String ALGO = "AES";
    private static final String TAG = "KDCS";

    interface OnSessionKeyUpdatedCallback {
        void onSessionKeyUpdated();
    }

    // Initialize KDCS for currently authenticated user
    public KDCSController(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.user = sharedPreferences.getString(Constants.DATABASE_USERNAME, "");
        this.database = FirebaseFirestore.getInstance();
        this.sessionKeyMap = new HashMap<String, SecretKey>();
        // bonus: add a master key for encryption before storing on Firebase
    }

    // Create symmetric session keys for both chat users and store in Firestore
    // for specific chat session
    public void generateSessionKey(String recipient) throws Exception {
        // Create new session key if does not exist
        this.updateSessionKeyMap(recipient, () -> {
            if (this.sessionKeyMap.containsKey(recipient)) {
                try {
                    String keyStr = this.convertKeyToString(this.sessionKeyMap.get(recipient));
                    Log.d(TAG, "Session key already exists:\t" + keyStr);
                    this.sharedPreferences.edit().putString(Constants.CHAT_SESSION_KEY, keyStr).apply();
                } catch (Exception e) {
                    Log.d(TAG, "Error converting key to String" + e);
                }

            }
            else {
                try {
                    SecretKey k =  KeyGenerator.getInstance(ALGO).generateKey();
                    String keyStr = Base64.getEncoder().encodeToString(k.getEncoded());;
                    addSessionKeyToDB(this.user, recipient, keyStr);
                    addSessionKeyToDB(recipient, this.user, keyStr);
                    this.sharedPreferences.edit().putString(Constants.CHAT_SESSION_KEY, keyStr).apply();
                } catch (Exception e) {
                    Log.d(TAG, "Error generating session key" + e);
                }
            }
        });
    }

    public SecretKey getSessionKey(String recipient) {
        Log.d(TAG, "SessionKey Map:\t" + this.sessionKeyMap.toString() + "\trecipeint" + recipient);
        return this.sessionKeyMap.get(recipient);
    }

    public static String convertKeyToString(SecretKey key) throws Exception {
        byte[] rawData = key.getEncoded();
        String encodedKey = Base64.getEncoder().encodeToString(rawData);
        return encodedKey;
    }

    public static SecretKey convertStringToKey(String keyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return key;
    }

    private void updateSessionKeyMap(String recipient, OnSessionKeyUpdatedCallback listener) throws Exception {
        this.getSessionKeyFromDB(recipient)
                .addOnSuccessListener(keyStr -> {
                    if (keyStr != null) {
                        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
                        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGO);
                        this.sessionKeyMap.put(recipient, key);
                        Log.d(TAG, "Session key map updated:\t" + this.sessionKeyMap.toString());
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
