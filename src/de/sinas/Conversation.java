package de.sinas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A normal conversation with two users
 */
public class Conversation {
	private String id;
	private ArrayList<Message> messages = new ArrayList<>();
	private String user1;
	private String user2;

	/**
	 * Creates a new conversation object.
	 * 
	 * @param id    the conversation's id
	 * @param user1 the first user
	 * @param user2 the second user
	 */
	public Conversation(String id, String user1, String user2) {
		this.id = id;
		this.user1 = user1;
		this.user2 = user2;
	}

	public void setUser1(String user1) {
		this.user1 = user1;
	}

	public void setUser2(String user2) {
		this.user2 = user2;
	}

	public String getUser1() {
		return user1;
	}

	public String getUser2() {
		return user2;
	}

	/**
	 * Adds one or more messages to the conversation. The messages in this conversation will be
	 * sorted by time stamp.
	 * 
	 * @param messages The message(s) to add
	 */
	public void addMessages(Message... msgs) {
		for (int i = 0; i < msgs.length; i++) {
			messages.add(msgs[i]);
		}
		messages.sort((m1, m2) -> (int) Math.signum(m1.getTimestamp() - m2.getTimestamp()));
	}

	public String getId() {
		return id;
	}

	/**
	 * Returns an unmodifiable list containing all messages in this conversation.
	 * 
	 * @return the messages
	 */
	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	/**
	 * Returns the user in this conversation that is not been given.
	 * 
	 * @return the other user. {@code null} if the given user is not in this
	 *         conversation
	 */
	public String getOtherUser(String user) {
		if (user.equals(user1))
			return user2;
		else if (user.equals(user2))
			return user1;
		else
			return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Conversation && ((Conversation) obj).id.equals(id);
	}
}
