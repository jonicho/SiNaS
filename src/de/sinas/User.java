package de.sinas;


public class User {
	private String ip;
	private int port;
	private String username;
	private String nickname;
	
	public User(String ip, int port, String username, String nickname) {
		this.ip = ip;
		this.port = port;
		this.username = username;
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
}
