package de.sinas.server;

import java.util.ArrayList;

import de.sinas.User;

public class Users {
	private ArrayList<User> users = new ArrayList<User>();

	public User getUser(String pClientIP, int pClientPort) {
		for (User user : users) {
			if (user.getIp().equals(pClientIP) && user.getPort() == pClientPort) {
				return user;
			}
		}
		return null;
	}
	
}
