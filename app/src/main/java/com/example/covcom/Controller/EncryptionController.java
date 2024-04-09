package com.example.covcom.Controller;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class EncryptionController {

    private SecretKey secretKey;
    private final String ALGO = "AES/CBC/PKCS5Padding";

    public EncryptionController(SecretKey secretKey) {
        Log.d("Encryption", "SecretKey:\t" + secretKey);
        this.secretKey = secretKey;
    }

    public String encrypt(String content) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGO);
        byte[] ivBytes = new byte[cipher.getBlockSize()];
        new SecureRandom().nextBytes(ivBytes); // Generate random IV
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, iv);
        byte[] encryptedBytes = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[ivBytes.length + encryptedBytes.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(encryptedBytes, 0, combined, ivBytes.length, encryptedBytes.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String cipherContent) throws Exception {
        byte[] combined = Base64.getDecoder().decode(cipherContent);
        Cipher cipher = Cipher.getInstance(ALGO);
        int ivLength = cipher.getBlockSize();
        IvParameterSpec iv = new IvParameterSpec(combined, 0, ivLength);
        cipher.init(Cipher.DECRYPT_MODE, this.secretKey, iv);
        byte[] decryptedBytes = cipher.doFinal(combined, ivLength, combined.length - ivLength);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
