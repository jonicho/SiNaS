package de.sinas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class managing users to ensure that there is no user twice.
 */
public class Users {
	private ArrayList<User> users = new ArrayList<User>();

	/**
	 * @return whether the user with the given ip and port exists
	 */
	public boolean doesUserExist(String clientIP, int clientPort) {
		return getUser(clientIP, clientPort) != null;
	}

	/**
	 * @return whether the user with the given username exists
	 */
	public boolean doesUserExist(String username) {
		return getUser(username) != null;
	}

	/**
	 * @return The user with the given ip and port. {@code null} if such a user does
	 *         not exist
	 */
	public User getUser(String clientIP, int clientPort) {
		for (User user : users) {
			if (user.getIp().equals(clientIP) && user.getPort() == clientPort) {
				return user;
			}
		}
		return null;
	}

	/**
	 * @return The user with the given username. {@code null} if such a user does
	 *         not exist
	 */
	public User getUser(String username) {
		for (User user : users) {
			if (user.getUsername().equals(username)) {
				return user;
			}
		}
		return null;
	}

	public List<User> getLoggedInUsers(List<String> usernames) {
		return usernames.stream().map(username -> getUser(username)).filter(user -> user != null).collect(Collectors.toList());
	}

	/**
	 * Adds a user to the list of users
	 * 
	 * @param user
	 * @throws IllegalArgumentException if a user with the same ip and port already
	 *                                  exists
	 */
	public void addUser(User user) throws IllegalArgumentException {
		if (doesUserExist(user.getIp(), user.getPort()) || doesUserExist(user.getUsername())) {
			throw new IllegalArgumentException("The user " + user + " does already exist!");
		}
		users.add(user);
	}

	/**
	 * Removes the given user
	 * 
	 * @return {@code true} if the specified user existed
	 */
	public boolean removeUser(User user) {
		return users.remove(user);
	}
}
