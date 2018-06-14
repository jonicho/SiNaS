package de.sinas;

public class User {
	private String ip;
	private int port;
	private String username;
	private String nickname;
	private boolean isAuthed;
	
	public User(String ip, int port, String username, String nickname) {
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.nickname = nickname;
		
	}
	
	public boolean isAuthed() {
		return isAuthed;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setAuthed(boolean isAuthed) {
		if(!isAuthed) {
			this.isAuthed = isAuthed;
		}
		else throw new IllegalArgumentException("Tried to deauthorize the User!");
		
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
}