package de.sinas.crypto;

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
    private int SEED_SIZE = 2048;
    private final int PBKDF2_ITERATIONS = 1000;
    private final int PBKDF2_SIZE = 128;

    public HashHandler() {
        try {
            sha = MessageDigest.getInstance("SHA-512");
            prng = SecureRandom.getInstance("SHA1PRNG");
            hgen = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    public void randomizePRNG() throws NoSuchAlgorithmException {
        SecureRandom seedRNG = SecureRandom.getInstance("SHA1PRNG");
        prng.setSeed(seedRNG.generateSeed(SEED_SIZE));
    }

    public byte[] getSHA512Hash(byte[] pInput) {
        return sha.digest(pInput);
    }

    public byte[] getPBKDF2Hash(String pInput,byte[] pSalt) throws InvalidKeySpecException {
        PBEKeySpec pbSpec = new PBEKeySpec(pInput.toCharArray(), pSalt, PBKDF2_ITERATIONS, PBKDF2_SIZE);
        return hgen.generateSecret(pbSpec).getEncoded();
    }

    public byte[] getSHA1PRNGBytes(int bSize) {
        byte[] ret = new byte[bSize];
        prng.nextBytes(ret);
        return ret;
    }
}