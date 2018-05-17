package de.sinas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GroupConversation {
	private String id;
	private String name;
	private ArrayList<Message> messages = new ArrayList<>();
	private ArrayList<User> users = new ArrayList<>();

	public GroupConversation(String id, User... users) {
		this.id = id;
		this.users.addAll(Arrays.asList(users));
	}
	
	public void addUser(User user) {
		users.add(user);
	}

	public void addMessage(Message message) {
		messages.add(message);
		messages.sort((m1, m2) -> (int) Math.signum(m1.getTimestamp() - m2.getTimestamp()));
	}

	public String getId() {
		return id;
	}

	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}
	
	public List<User> getUsers() {
		return Collections.unmodifiableList(users);
	}
	
	public String getName() {
		return name;
	}
}
