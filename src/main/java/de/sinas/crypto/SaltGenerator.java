package de.sinas.crypto;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

public class SaltGenerator {

    private SaltGenerator() {
    }

    public static byte[] generateSalt(String username, String password, HashHandler hashHandler) {
        byte[] uHash = hashHandler.getCheckSum(username.getBytes());
        byte[] pHash = hashHandler.getCheckSum(password.getBytes());
        byte[][] xorTable = new byte[16][uHash.length];
        xorTable[0] = uHash;
        for (int i = 0; i < xorTable.length; i++) {
            if (i % 2 == 0) {
                xorTable[i] = uHash;
            } else {
                xorTable[i] = pHash;
            }
        }
        byte[] roundKey = pHash;
        for (int i = 0; i < 16; i++) {
            for (byte[] arr : xorTable) {
                for (byte b : arr) {
                    int byteIndex = ArrayUtils.indexOf(arr, b);
                    int rowIndex = ArrayUtils.indexOf(xorTable, arr);
                    if (byteIndex > 1 && byteIndex < uHash.length) {
                        b = xor(b, arr[byteIndex - 1]);
                    }
                    if (i > 1 && rowIndex > 1) {
                        b = xor(b, xorTable[rowIndex - 1][byteIndex]);
                        arr = xorArray(xorTable[rowIndex - 1], arr);
                    }
                }
            }
            roundKey = xorArray(roundKey, xorTable[i]);
        }
        byte state = 0;
        int stepCounter = 0;
        ArrayList<Byte> derivate = new ArrayList<Byte>();
        for (byte[] arr : xorTable) {
            for (byte b : arr) {
                state += b;
                state %= 256;
                if (stepCounter == 4) {
                    derivate.add(state);
                    state = 0;
                    stepCounter = 0;
                } else {
                    stepCounter++;
                }
            }
        }
        byte[] result = new byte[derivate.size()];
        int count = 0;
        for (byte b : derivate) {
            result[count] = b;
            count++;
        }
        return result;
    }

    private static byte[] xorArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            c[i] = (byte) ((a[i] ^ b[i]) & 0x000000ff);
        }
        return c;
    }

    private static byte xor(byte a, byte b) {
        byte c = (byte) ((a ^ b) & 0x000000ff);
        return c;
    }
}