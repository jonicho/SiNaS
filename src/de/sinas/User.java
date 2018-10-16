package de.sinas;

/**
 * A user with an ip, a port, a username and a nickname
 */
public class User {
	private String ip;
	private int port;
	private String username;

	/**
	 * Creates a new user with the given ip, port, username and nickname
	 */
	public User(String ip, int port, String username) {
		this.ip = ip;
		this.port = port;
		this.username = username;
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

	@Override
	public boolean equals(Object obj) {
		return obj instanceof User && ((User) obj).getUsername().equals(getUsername())
				&& ((User) obj).getIp().equals(getIp()) && ((User) obj).getPort() == getPort();
	}

	@Override
	public String toString() {
		return username + "@" + ip + ":" + port;
	}
}
