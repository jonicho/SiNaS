package de.sinas.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;

import de.sinas.Conversation;
import de.sinas.Message;
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
	 * @param databaseDirectory
	 *            the directory in which the data is to be stored
	 * @throws IllegalArgumentException
	 *             when given database directory is not a directory
	 */
	public Database(File databaseDirectory) throws IllegalArgumentException {
		if (!databaseDirectory.exists()) {
			databaseDirectory.mkdir();
			File[] structure = { new File(databaseDirectory + "/conversations"), new File(databaseDirectory + "/files"),
					new File(databaseDirectory + "/users") };
			for (File folder : structure) {
				folder.mkdir();
			}
		}
		if (!databaseDirectory.isDirectory()) {
			throw new IllegalArgumentException("Database directory has to be a directory!");
		}
		this.databaseDirectory = databaseDirectory;
	}

	/**
	 * Loads the user with the given name with ip and port.</br>
	 * This method is to get a user that connected to the server.</br>
	 * If no user with the given name exists a new one is created.
	 * 
	 * @return The user with the given username
	 */
	public User getConnectedUser(String username, String ip, int port) {
		String nickname;
		File file = new File(databaseDirectory + "/users/ " + username + ".txt");
		
		if(!file.exists()) {
			try {
				PrintWriter writer = new PrintWriter(file, "UTF-8");
				writer.println(username);
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			ArrayList<String> lines = new ArrayList<>();
			while (reader.readLine() != null) {
				lines.add(reader.readLine());
			}
			reader.close();
			nickname = lines.get(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		

		return new User(ip, port, username, nickname);
	}

	/**
	 * Loads the user with the given name without ip and port.</br>
	 * This method is to get information about a user without having the user to
	 * connect to the server.</br>
	 * If no user with the given name exists {@code null} is returned.
	 * 
	 * @return The user with the given username. {@code null} if no user with the
	 *         given name exists.
	 */
	public User getUserInfo(String username) {
		String nickname;
		File file = new File(databaseDirectory + "/users/ " + username + ".txt");
		if(!file.exists()) {
			return null;
		} else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				ArrayList<String> lines = new ArrayList<>();
				while (reader.readLine() != null) {
					lines.add(reader.readLine());
				}
				reader.close();
				nickname = lines.get(0);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return new User("", 0, username, nickname);
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
				if (conversationInformation[0].equals(user.getUsername())) {
					conversations.add(new Conversation(filelist[i].getName(), user.getUsername(), getUserInfo(conversationInformation[1]).getUsername()));
				} else if (conversationInformation[1].equals(user.getUsername())) {
					conversations.add(new Conversation(filelist[i].getName(), user.getUsername(), getUserInfo(conversationInformation[0]).getUsername()));
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return conversations;
	}

	/**
	 * Saves the given user
	 */
	public void saveUser(User user) {
		File file = new File(databaseDirectory + "/users/" + user.getUsername() + ".txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.println(user.getNickname());
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the given conversation
	 */
	public void saveConversation(Conversation conversation) {
		File file = new File(databaseDirectory + "/conversations/" + conversation.getId() + ".txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			
			String conversationInformation = conversation.getUser1() + ":" + conversation.getUser2() ;
			writer.write(conversationInformation);
			
			for(Message message : conversation.getMessages()) {
				String messageInformation = message.getId() + ":" + message.getTimestamp() + ":" + message.getSender() + ":" + message.isFile() + ":" + message.getContent();
				writer.write(messageInformation);
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
