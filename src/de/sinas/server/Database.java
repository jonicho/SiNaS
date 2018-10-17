package de.sinas.server;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;

import java.io.*;
import java.sql.*;
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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `users` ( `username` TEXT NOT NULL UNIQUE, `password` TEXT NOT NULL, PRIMARY KEY(`username`) )");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `conversations` ( `conversation_id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `name` TEXT NOT NULL )");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `messages` ( `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `sender` TEXT NOT NULL, `is_file` REAL NOT NULL, `conversation_id` INTEGER NOT NULL, FOREIGN KEY(`sender`) REFERENCES `users`(`username`), FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`conversation_id`) )");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `conversations_users` ( `conversation_id` INTEGER NOT NULL, `username` TEXT NOT NULL, FOREIGN KEY(`conversation_id`) REFERENCES `conversations`(`conversation_id`), FOREIGN KEY(`username`) REFERENCES `users`(`username`), PRIMARY KEY(`conversation_id`,`username`) )");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the user with the given name with ip and port.<br>
	 * This method is to get a user that connected to the server.<br>
	 * If no user with the given name exists a new one is created.
	 *
	 * @param ip
	 * @param port
	 * @param username
	 * @return The user with the given username
	 */
	public User getConnectedUser(String username, String ip, int port) {
		try {
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM `users` WHERE `username`=?");
			statement.setString(1, username);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return new User(ip, port, rs.getString("username"), rs.getString("password"));
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Loads the user with the given name with an empty ip and the port 0.<br>
	 * This method is to get information about a user without having the user to
	 * connect to the server.<br>
	 * If no user with the given name exists {@code null} is returned.
	 *
	 * @param username
	 * @return The user with the given username. {@code null} if no user with the
	 * given name exists.
	 */
	public User getUserInfo(String username) {
		return getConnectedUser(username, "", 0);
	}

	/**
	 * Loads all conversations of the given user
	 *
	 * @param user
	 * @return all conversations of the given user
	 */
	public ArrayList<Conversation> getConversations(User user) {
		return null;
	}

	/**
	 * Loads the conversation with the given conversation id
	 *
	 * @param conversationId
	 * @return
	 */
	public Conversation getConversation(String conversationId) {
		try {
			String id;
			String name;
			ArrayList<String> users = new ArrayList<>();
			{
				PreparedStatement statement = connection.prepareStatement("SELECT * FROM conversations WHERE conversation_id=?");
				statement.setString(1, conversationId);
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {
					id = rs.getString("conversation_id");
					name = rs.getString("name");
				} else {
					return null;
				}
			}
			{
				PreparedStatement statement = connection.prepareStatement("SELECT * FROM conversations_users WHERE conversation_id=?");
				statement.setString(1, conversationId);
				ResultSet rs = statement.executeQuery();
				while (rs.next()) {
					users.add(rs.getString("username"));
				}
			}
			return new Conversation(id, name, users.toArray(new String[0]));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates the given user in the database
	 *
	 * @param user
	 * @return true if the user did not already exist, false otherwise
	 */
	public boolean createUser(User user) {
		return false;
	}

	/**
	 * Updates the given user in the database
	 *
	 * @param user
	 * @return true if the user exists, false otherwise
	 */
	public boolean updateUser(User user) {
		return false;
	}

	/**
	 * Creates the given conversation in the database<br>
	 * (NOTE: this method only creates the conversation, not the conversation's messages;<br>
	 *       to create messages use {@link #createMessage(Message)})
	 *
	 * @param conversation
	 * @return true if the conversation did not already exist, false otherwise
	 */
	public boolean createConversation(Conversation conversation) {
		return false;
	}

	/**
	 * Updates the given conversation in the database<br>
	 * (NOTE: this method only updates the conversation, not the conversation's messages;<br>
	 *       to update messages use {@link #updateMessage(Message)})
	 *
	 * @param conversation
	 * @return true if the conversation exists, false otherwise
	 */
	public boolean updateConversation(Conversation conversation) {
		return false;
	}

	/**
	 * Creates the given message in the database
	 *
	 * @param message
	 * @return true if the message did not already exist, false otherwise
	 */
	public boolean createMessage(Message message) {
		return false;
	}

	/**
	 * Updates the given message in the database
	 *
	 * @param message
	 * @return true if the message exists, false otherwise
	 */
	public boolean updateMessage(Message message) {
		return false;
	}
}
