package de.sinas.server;

import java.security.Key;
import java.security.PublicKey;

import javax.crypto.SecretKey;

public class TempUser {
    private String ip;
    private int port;
    private SecretKey aesKey;
    private SecretKey rsaKey;

    public TempUser(String pClientIP,int pClientPort) {
        ip = pClientIP;
        port = pClientPort;
    }

    /**
     * @return the aesKey
     */
    public SecretKey getAesKey() {
        return aesKey;
    }
    
    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the rsaKey
     */
    public SecretKey getRsaKey() {
        return rsaKey;
    }

    /**
     * @param aesKey the aesKey to set
     */
    public void setAesKey(SecretKey aesKey) {
        this.aesKey = aesKey;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @param rsaKey the rsaKey to set
     */
    public void setRsaKey(SecretKey rsaKey) {
        this.rsaKey = rsaKey;
    }
}