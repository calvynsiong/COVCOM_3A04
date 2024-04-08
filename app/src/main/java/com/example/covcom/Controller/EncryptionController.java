package com.example.covcom.Controller;

import android.util.Log;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class EncryptionController {

    private SecretKey secretKey;
    private final String ALGO = "AES";
    public EncryptionController (SecretKey secretKey) {
        Log.d("Encryption", "SecretKey:\t" + secretKey);
        this.secretKey = secretKey;
    }

    public String encrypt(String content) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
        byte[] encryptedBytes = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String cipherContent) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(cipherContent));
        return new String(decryptedBytes);
    }

}
