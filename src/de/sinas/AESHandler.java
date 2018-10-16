package de.sinas;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AESHandler {
    private Cipher cipher;
    private KeyPairGenerator kpg;
    private final int KEY_SIZE = 4096;

    public AESHandler() { 
        try {
            cipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            ex.printStackTrace();
        }
    }

    public KeyPair generateKey() {
        return null;
    }

    public byte[] encrypt(byte[] input, PublicKey pKey) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, pKey);  
            return cipher.doFinal(input);   
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] decrypt(byte[] input, PrivateKey pKey) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, pKey);  
            return cipher.doFinal(input);   
        } catch(InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}