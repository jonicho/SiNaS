package de.sinas.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESHandler {
    private Cipher cipher;
    private final int KEY_SIZE = 32;
    private final int SEED_SIZE = 1024;
    private SecureRandom prng;

    public AESHandler() { 
        try {
            cipher = Cipher.getInstance("AES");
            prng = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            ex.printStackTrace();
        }
    }

    public void randomizePRNG() throws NoSuchAlgorithmException {
        SecureRandom seedRNG = SecureRandom.getInstance("SHA1PRNG");
        prng.setSeed(seedRNG.generateSeed(SEED_SIZE));
    }

    public SecretKey generateKey() {
        byte[] keyBytes = new byte[KEY_SIZE];
        prng.nextBytes(keyBytes);
        System.out.println(Arrays.toString(keyBytes));
        SecretKey sKey = new SecretKeySpec(keyBytes, "AES");
        return sKey;
    }

    public byte[] encrypt(byte[] input, SecretKey pKey) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, pKey);  
            return cipher.doFinal(input);   
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] input, SecretKey pKey) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, pKey); 
            byte [] decBytes = cipher.doFinal(input);
            System.out.println("(AES)"+new String(decBytes));
            return decBytes;   
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}