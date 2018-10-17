package de.sinas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A conversation containing an arbitrary amount of users
 */
public class Conversation {
	private String id;
	private String name;
	private ArrayList<Message> messages = new ArrayList<>();
	private ArrayList<String> users = new ArrayList<>();

	/**
	 * Creates a new conversation with an arbitrary amount of users.
	 * 
	 * @param id    the conversation id
	 * @param users an arbitrary amount of users
	 */
	public Conversation(String id, String name, String... users) {
		this.id = id;
		this.name = name;
		this.users.addAll(Arrays.asList(users));
	}

	/**
	 * Creates a new conversation with a new id and an arbitrary amount of users.
	 * 
	 * @param users an arbitrary amount of users
	 */
	public Conversation(String name, String... users) {
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.users.addAll(Arrays.asList(users));
	}

	/**
	 * Renames this conversation
	 */
	public void rename(String name) {
		this.name = name;
	}

	/**
	 * Removes the given user
	 * 
	 * @param username the username of the user to remove
	 * @return {@code true} if this conversation contained the specified user
	 */
	public boolean removeUser(String username) {
		return users.remove(username);
	}

	public void addUser(String user) {
		users.add(user);
	}

	/**
	 * Adds one or more messages to the conversation. The messages in this
	 * conversation will be sorted by time stamp.
	 * 
	 * @param messages The message(s) to add
	 */
	public void addMessages(Message... msgs) {
		for (int i = 0; i < msgs.length; i++) {
			if (!messages.contains(msgs[i])) {
				messages.add(msgs[i]);
			}
		}
		messages.sort((m1, m2) -> (int) Math.signum(m1.getTimestamp() - m2.getTimestamp()));
	}

	public boolean contains(String user) {
		for (String u : users) {
			if (u.equals(user)) {
				return true;
			}
		}
		return false;
	}

	public String getId() {
		return id;
	}

	/**
	 * @return the name of this conversation
	 */
	public String getName() {
		return name;
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
	public List<String> getUsers() {
		return Collections.unmodifiableList(users);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Conversation && ((Conversation) obj).getId().equals(id);
	}
}
