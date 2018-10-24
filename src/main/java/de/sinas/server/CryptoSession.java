package de.sinas.server;

import de.sinas.User;

import javax.crypto.SecretKey;

public class CryptoSession {
	private SecretKey userPublicKey;
	private SecretKey mainAESKey;
	private User owner;

	public CryptoSession(User u, SecretKey userPublicKey, SecretKey mainAESKey) {
		owner = u;
		this.userPublicKey = userPublicKey;
		this.mainAESKey = mainAESKey;
	}

	/**
	 * @return the owner
	 */
	public User getOwner() {
		return owner;
	}

	/**
	 * @return the mainAESKey
	 */
	public SecretKey getMainAESKey() {
		return mainAESKey;
	}

	/**
	 * @return the userPublicKey
	 */
	public SecretKey getUserPublicKey() {
		return userPublicKey;
	}


}