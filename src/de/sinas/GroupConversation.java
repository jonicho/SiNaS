package de.sinas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A group conversation containing an arbitrary amount of users
 */
public class GroupConversation {
	private String id;
	private String name;
	private ArrayList<Message> messages = new ArrayList<>();
	private ArrayList<User> users = new ArrayList<>();

	/**
	 * Creates a new group conversation with an id and an arbitrary amount of users.
	 * 
	 * @param id
	 *            the conversation id
	 * @param users
	 *            an arbitrary amount of users
	 */
	public GroupConversation(String id, User... users) {
		this.id = id;
		this.users.addAll(Arrays.asList(users));
	}

	public void addUser(User user) {
		users.add(user);
	}

	/**
	 * Adds a message to the conversation. The messages in this conversation will be
	 * sorted by time stamp.
	 * 
	 * @param message The message to add
	 */
	public void addMessage(Message message) {
		messages.add(message);
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
	 * Returns an unmodifiable list containing all users in this conversation.
	 * 
	 * @return the users
	 */
	public List<User> getUsers() {
		return Collections.unmodifiableList(users);
	}

	/**
	 * @return the name of this group conversation
	 */
	public String getName() {
		return name;
	}
}
