package de.sinas.server;

import javax.crypto.SecretKey;

public class TempUser {
	private String ip;
	private int port;
	private SecretKey aesKey;
	private SecretKey rsaKey;

	public TempUser(String pClientIP, int pClientPort) {
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
	 * @param aesKey the aesKey to set
	 */
	public void setAesKey(SecretKey aesKey) {
		this.aesKey = aesKey;
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
	 * @param rsaKey the rsaKey to set
	 */
	public void setRsaKey(SecretKey rsaKey) {
		this.rsaKey = rsaKey;
	}
}