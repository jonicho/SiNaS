package de.sinas.server;

import de.sinas.User;

import java.security.PublicKey;

import javax.crypto.SecretKey;

public class CryptoSession {
	private PublicKey userPublicKey;
	private SecretKey mainAESKey;
	private User owner;

	public CryptoSession(User u, PublicKey userPublicKey, SecretKey mainAESKey) {
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
	public PublicKey getUserPublicKey() {
		return userPublicKey;
	}


}