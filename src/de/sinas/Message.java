package de.sinas;

public class Message {
	private String id;
	private String content;
	private long timestamp;
	private User sender;
	private boolean isFile;

	public Message(String content, long timestamp, User sender, boolean isFile) {
		this.content = content;
		this.timestamp = timestamp;
		this.sender = sender;
		this.isFile = isFile;
	}

	public String getContent() {
		return content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public User getSender() {
		return sender;
	}

	public boolean isFile() {
		return isFile;
	}
	
	public String getId() {
		return id;
	}
}
