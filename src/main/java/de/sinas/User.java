package de.sinas;

/**
 * A user with an ip, a port, a username and a nickname
 */
public class User {
	private String ip;
	private int port;
	private String username;
	private String passwordHash;

	/**
	 * Creates a new user with the given ip, port, username and passwordHash
	 */
	public User(String ip, int port, String username, String passwordHash) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.passwordHash = passwordHash;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * @param passwordHash the passwordHash to set
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public boolean isTempUser() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof User
				&& ((User) obj).getUsername().equals(getUsername())
				&& ((User) obj).getIp().equals(getIp())
				&& ((User) obj).getPort() == getPort()
				&& ((User) obj).getPasswordHash().equals(getPasswordHash());
	}

	@Override
	public String toString() {
		return username + "@" + ip + ":" + port;
	}
}
