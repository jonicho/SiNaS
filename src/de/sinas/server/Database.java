package de.sinas.server;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Database {
	private Connection connection;

	public Database(String dbPath) {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initEmptyDatabase();
	}

	private void initEmptyDatabase() {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `users` ( `username` TEXT UNIQUE, `password` TEXT, PRIMARY KEY(`username`) )");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `conversations` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, `name` TEXT )");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `messages` ( `id` INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, `content` TEXT, `timestamp` INTEGER, `sender` TEXT, `is_file` INTEGER, `conversation_id` INTEGER, FOREIGN KEY(`sender`) REFERENCES `users`(`username`), FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`id`) )");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `conversations_users` ( `conversation_id` INTEGER UNIQUE, `username` TEXT UNIQUE, FOREIGN KEY(`username`) REFERENCES `users`(`username`), FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`id`), PRIMARY KEY(`conversation_id`,`username`) )");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the user with the given name with ip and port.<br>
	 * This method is to get a user that connected to the server.<br>
	 * If no user with the given name exists a new one is created.
	 *
	 * @return The user with the given username
	 */
	public User getConnectedUser(String username, String ip, int port) {
		return null;
	}

	/**
	 * Loads the user with the given name with an empty ip and the port 0.<br>
	 * This method is to get information about a user without having the user to
	 * connect to the server.<br>
	 * If no user with the given name exists {@code null} is returned.
	 *
	 * @return The user with the given username. {@code null} if no user with the
	 * given name exists.
	 */
	public User getUserInfo(String username) {
		return null;
	}

	/**
	 * Loads all conversations of the given user
	 *
	 * @return all conversations of the given user
	 */
	public ArrayList<Conversation> getConversations(User user) {
		return null;
	}

	/**
	 * Saves the given user
	 */
	public void saveUser(User user) {
	}

	/**
	 * Saves the given conversation
	 */
	public void saveConversation(Conversation conversation) {
	}
}
