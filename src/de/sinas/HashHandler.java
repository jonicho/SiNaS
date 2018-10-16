package de.sinas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class HashHandler {

    private MessageDigest sha;
    private SecureRandom prng;
    private SecretKeyFactory hgen;

    public HashHandler() {
        try {
            sha = MessageDigest.getInstance("SHA-512");
            prng = SecureRandom.getInstance("SHA1PRNG");
            hgen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getCheckSum(byte[] pInput) {
        return sha.digest(pInput);
    }

    public byte[] getSecureHash(String pInput,byte[] pSalt,int iterations, int hSize) throws InvalidKeySpecException {
        PBEKeySpec pbSpec = new PBEKeySpec(pInput.toCharArray(), pSalt, iterations, hSize);
        return hgen.generateSecret(pbSpec).getEncoded();
    }

    public byte[] getSecureRandomBytes(int bSize) {
        byte[] ret = new byte[bSize];
        prng.nextBytes(ret);
        return ret;
    }
}