package de.sinas.crypto;

import java.util.Base64;

public final class Encoder {

    public static String b64Encode(byte[] pInput) {
        byte[] enc = Base64.getEncoder().encode(pInput);
        return new String(enc);
    }

    public static byte[] b64Decode(String pInput) {
        return Base64.getDecoder().decode(pInput);
    }
}