package de.sinas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Conversation {
	private String id;
	private ArrayList<Message> messages = new ArrayList<>();
	private ArrayList<User> users = new ArrayList<>();

	public Conversation(String id, User... users) {
		this.id = id;
		this.users.addAll(Arrays.asList(users));
	}

	public void addUser(User user) {
		users.add(user);
	}

	public void addMessage(Message message) {
		messages.add(message);
	}

	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public List<User> getUsers() {
		return Collections.unmodifiableList(users);
	}
}
