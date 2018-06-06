package de.sinas.server;

import java.util.ArrayList;

import de.sinas.User;

public class Users {
	private ArrayList<User> users = new ArrayList<User>();

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

}
