package de.sinas;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * A message that can represent a plain text message or a file message, in which
 * case the content attribute contains the file's server-side location.
 *
 */
public class Message {
	private String id;
	private String content;
	private long timestamp;
	private String sender;
	private boolean isFile;

	/**
	 * Creates a new message
	 * 
	 * @param id        id from database
	 * @param content   the message content
	 * @param timestamp the time at which the message was sent
	 * @param sender    the user that sent the message
	 * @param isFile    whether the message represents a file
	 */
	public Message(String id, String content, long timestamp, String sender, boolean isFile) {
		this.content = content;
		this.timestamp = timestamp;
		this.sender = sender;
		this.isFile = isFile;
		this.id = id;
	}

	/**
	 * Creates a new message with a new id.
	 * 
	 * @param content   the message content
	 * @param timestamp the time at which the message was sent
	 * @param sender    the user that sent the message
	 * @param isFile    whether the message represents a file
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public Message(String content, long timestamp, String sender, boolean isFile)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		this.content = content;
		this.timestamp = timestamp;
		this.sender = sender;
		this.isFile = isFile;
		byte[] stringBytes = (content + timestamp + sender + isFile).getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] hashBytes = md.digest(stringBytes);
		byte[] encodedBytes = Base64.getEncoder().encode(hashBytes);
		id = new String(encodedBytes);
	}

	/**
	 * Returns this message's content which is plain text or, when this message
	 * represents a file, the file's server-side location
	 * 
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getSender() {
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
