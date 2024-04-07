package com.example.covcom.Controller;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.covcom.Constants;
import com.google.firebase.firestore.SetOptions;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;

public class KDCSController {
    private String user;
    private FirebaseFirestore database;
    private static final String ALGO = "AES";
    private static final String TAG = "KDCS";

    // Initialize KDCS for currently authenticated user
    public KDCSController(String user) {
        this.user = user;
        this.database = FirebaseFirestore.getInstance();
        // bonus: add a master key for encryption before storing on Firebase
    }

    // Create symmetric session keys for both chat users and store in Firestore
    // for specific chat session
    public void generateSessionKey(String recipient) throws Exception {
        SecretKey k =  KeyGenerator.getInstance(ALGO).generateKey();
        String keyStr = Base64.getEncoder().encodeToString(k.getEncoded());;
        addSessionKeyToDB(this.user, recipient, keyStr);
        addSessionKeyToDB(recipient, this.user, keyStr);
    }

    public SecretKey getSessionKey(String recipient) throws Exception {
        String keyStr = getSessionKeyFromDB(recipient);
        if (keyStr.isEmpty()) {
            byte[] decodedKey = Base64.getDecoder().decode(keyStr);
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGO);
        }
//        else {
//            throw new Exception("Key not retrieved");;
//        }
        return new SecretKeySpec(new byte[0], 0, 0, ALGO);
    }

    // Update Firestore session key for user
    private void addSessionKeyToDB(String sender, String recipient, String key) {
        HashMap<String, String> recipientKeyPair = new HashMap<String, String>();
        recipientKeyPair.put(recipient, key);
        this.database.collection(Constants.KDCS).document(sender)
                .set(recipientKeyPair, SetOptions.merge())
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

    private String getSessionKeyFromDB(String recipient) {
        String sessionKey = "";
        this.database.collection(Constants.KDCS).document(this.user)
                .get()
                .continueWith(new Continuation<DocumentSnapshot, String>() {
                    @Override
                    public String then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                        if (task.isSuccessful()) {
                            // Document found in the offline cache
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "Cached document data: " + document.getData());
                            if (document.exists()) {
                                return document.getString(recipient);
                            } else {
                                throw new Exception("Document not found");
                            }

                        } else {
                            Log.d(TAG, "Cached get failed: ", task.getException());
                            throw(task.getException());
                        }
                    }
                });
        return sessionKey;
    }
}
