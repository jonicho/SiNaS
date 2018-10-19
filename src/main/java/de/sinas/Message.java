package de.sinas;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * A message that can represent a plain text message or a file message, in which
 * case the content attribute contains the file's server-side location.
 */
public class Message {
	private String id;
	private String content;
	private long timestamp;
	private String sender;
	private boolean isFile;
	private String conversationId;

	/**
	 * Creates a new message
	 *
	 * @param id             id from database
	 * @param content        the message content
	 * @param timestamp      the time at which the message was sent
	 * @param sender         the user that sent the message
	 * @param isFile         whether the message represents a file
	 * @param conversationId the id of the conversation in which this message is in
	 */
	public Message(String id, String content, long timestamp, String sender, boolean isFile, String conversationId) {
		this.content = content;
		this.timestamp = timestamp;
		this.sender = sender;
		this.isFile = isFile;
		this.id = id;
		this.conversationId = conversationId;
	}

	/**
	 * Creates a new message and generates an id by hashing the following
	 * string:<br>
	 * (content + timestamp + sender + isFile)
	 *
	 * @param content        the message content
	 * @param timestamp      the time at which the message was sent
	 * @param sender         the user that sent the message
	 * @param isFile         whether the message represents a file
	 * @param conversationId the id of the conversation in which this message is in
	 * @throws NoSuchAlgorithmException
	 */
	public Message(String content, long timestamp, String sender, boolean isFile, String conversationId)
			throws NoSuchAlgorithmException {
		this(new String(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest((content + timestamp + sender + isFile).getBytes(StandardCharsets.UTF_8)))), content, timestamp, sender, isFile, conversationId);
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

	public String getConversationId() {
		return conversationId;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Message && ((Message) obj).getId().equals(id);
	}
}
