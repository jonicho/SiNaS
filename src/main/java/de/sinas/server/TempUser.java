package de.sinas.server;

import de.sinas.User;

import javax.crypto.SecretKey;

public class TempUser extends User {
	private SecretKey aesKey;
	private SecretKey rsaKey;

	public TempUser(String pClientIP, int pClientPort) {
		super(pClientIP, pClientPort, null, null);
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

	@Override
	public String getUsername() {
		throw new IllegalStateException("a temporary user has no username!");
	}

	@Override
	public String getPasswordHash() {
		throw new IllegalStateException("a temporary user has no password hash!");
	}
}