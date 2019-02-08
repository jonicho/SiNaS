package de.sinas.crypto;

import java.util.ArrayList;

import com.sun.tools.javac.util.ArrayUtils;

public class SaltGenerator {

    private SaltGenerator() {
    }

    public static byte[] generateSalt(String username, String password, HashHandler hashHandler) {
        byte[] uHash = hashHandler.getCheckSum(username.getBytes());
        byte[] pHash = hashHandler.getCheckSum(password.getBytes());
        byte[][] xorTable = new byte[16][uHash.length];
        byte[] roundKey = pHash;
        for(int i= 0; i < 16; i++) {
            for(byte[] arr : xorTable) {
                for(byte b : arr) {
                    int byteIndex = org.apache.commons.lang3.ArrayUtils.indexOf(arr, b);
                    int rowIndex = org.apache.commons.lang3.ArrayUtils.indexOf(xorTable,arr);
                    if(byteIndex > 0 && byteIndex < uHash.length){
                        b = xor(b, arr[byteIndex-1]);
                    } 
                    if(i > 0) {
                        b = xor(b,xorTable[rowIndex-1][byteIndex]);
                        arr = xorArray(xorTable[rowIndex-1],arr);
                    }
                }
            }
            roundKey = xorArray(roundKey, xorTable[i]);
        }
        byte state = 0;
        int stepCounter = 0;
        ArrayList<Byte> derivate = new ArrayList<Byte>();
        for(byte[] arr : xorTable) {
            for(byte b : arr) {
                state += b;
                state %= 256;
                if(stepCounter == 4) {
                    derivate.add(b);
                    b = 0;
                    stepCounter = 0;
                } else {
                    stepCounter++;
                }
            }
        }
        byte[] result = new byte[derivate.size()];
        int count = 0;
        for(byte b : derivate) {
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