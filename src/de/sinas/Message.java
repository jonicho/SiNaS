package de.sinas;

/**
 * A message that can represent a plain text message or a file message, in which
 * case the content attribute contains the file's server-side location.
 *
 */
public class Message {
	private String id;
	private String content;
	private long timestamp;
	private User sender;
	private boolean isFile;

	/**
	 * Creates a new message
	 * 
	 * @param content
	 *            the message content
	 * @param timestamp
	 *            the time at which the message was sent
	 * @param sender
	 *            the user that sent the message
	 * @param isFile
	 *            whether the message represents a file
	 */
	public Message(String content, long timestamp, User sender, boolean isFile) {
		this.content = content;
		this.timestamp = timestamp;
		this.sender = sender;
		this.isFile = isFile;
	}

	/**
	 * Returns this message's content which is plain text or, when this message
	 * represents a file, the file's server-side localtion
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public User getSender() {
		return sender;
	}

	/**
	 * Returns whether this message represents a file
	 * 
	 * @return whether this message represents a file
	 */
	public boolean isFile() {
		return isFile;
	}

	public String getId() {
		return id;
	}
}
