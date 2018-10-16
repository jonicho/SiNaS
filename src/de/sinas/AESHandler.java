package de.sinas;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.SecretKeySpec;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.SecureRandom;

public class AESHandler {
    private Cipher cipher;
    private final int KEY_SIZE = 4096;
    private SecureRandom prng;

    public AESHandler() { 
        try {
            cipher = Cipher.getInstance("AES");
            prng = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            ex.printStackTrace();
        }
    }

    public SecretKeySpec generateKey() {
        byte[] keyBytes = new byte[KEY_SIZE];
        prng.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public byte[] encrypt(byte[] input, SecretKeySpec pKey) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, pKey);  
            return cipher.doFinal(input);   
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] input, SecretKeySpec pKey) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, pKey);  
            return cipher.doFinal(input);   
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}