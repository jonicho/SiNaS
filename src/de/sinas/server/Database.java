package de.sinas.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import de.sinas.Conversation;
import de.sinas.User;

/**
 * The database for SiNaS. Stores user and conversation data in the given
 * directory
 */
public class Database {
	private File databaseDirectory;

	/**
	 * Creates a new database object
	 * 
	 * @param databaseDirectory the directory in which the data is to be stored
	 * @throws IllegalArgumentException when given database directory is not a
	 *                                  directory
	 */
	public Database(File databaseDirectory) throws IllegalArgumentException {
		if (!databaseDirectory.isDirectory()) {
			throw new IllegalArgumentException("Database directory has to be a directory!");
		}
		this.databaseDirectory = databaseDirectory;
	}

	/**
	 * Loads the user with the given name
	 * 
	 * @return The user with the given username
	 */
	public User getUser(String username, String ip, int port) {
		return new User(ip, port, username, username); // TODO load user from database
	}

	/**
	 * Loads all conversations of the given user
	 * 
	 * @return all conversations of the given user
	 */
	public ArrayList<Conversation> getConversations(User user) {
		ArrayList<Conversation> conversations = new ArrayList<>();

		File[] filelist = databaseDirectory.listFiles();
		for (int i = 0; i < databaseDirectory.list().length; i++) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(filelist[i]));
				ArrayList<String> lines = new ArrayList<>();
				while (reader.readLine() != null) {
					lines.add(reader.readLine());
				}
				String[] conversationInformation = lines.get(0).split(":");
				if(conversationInformation[1].equals(user.getUsername()) || conversationInformation[2].equals(user.getUsername())) {
					conversations.add(new Conversation(conversationInformation[0], user.getUsername(), user.getUsername())); //TODO change to user1 and user2
					
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Saves the given user
	 */
	public void saveUser(User user) {
		File userfile = new File(databaseDirectory + "\\users\\" + user.getUsername() + ".txt");
		if (!userfile.exists()) {
			try {
				userfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the given conversation
	 */
	public void saveConversation(Conversation conversation) {
		File conversationfile = new File(databaseDirectory + "\\conversations\\" + conversation.getId() + ".txt");
		if (!conversationfile.exists()) {
			try {
				conversationfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
