package de.sinas;

/**
 * A user with an ip, a port, a username and a nickname
 */
public class User {
	private String ip;
	private int port;
	private String username;
	private String nickname;

	/**
	 * Creates a new user with the given ip, port, username and nickname
	 */
	public User(String ip, int port, String username, String nickname) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.nickname = nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
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

	public String getNickname() {
		return nickname;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof User && ((User) obj).getUsername().equals(getUsername())
				&& ((User) obj).getNickname().equals(getNickname()) && ((User) obj).getIp().equals(getIp())
				&& ((User) obj).getPort() == getPort();
	}

	@Override
	public String toString() {
		return username + "@" + ip + ":" + port;
	}
}
