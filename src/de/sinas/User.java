package de.sinas;

/**
 * A user with an ip, a port, a username and a nickname
 */
public class User {
	private String ip;
	private int port;
	private String username;
	private String password;

	/**
	 * Creates a new user with the given ip, port, username and password
	 */
	public User(String ip, int port, String username, String password) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
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

	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof User
				&& ((User) obj).getUsername().equals(getUsername())
				&& ((User) obj).getIp().equals(getIp())
				&& ((User) obj).getPort() == getPort()
				&& ((User) obj).getPassword().equals(getPassword());
	}

	@Override
	public String toString() {
		return username + "@" + ip + ":" + port;
	}
}
