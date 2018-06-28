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
 * 
 * user file structure (filename = username)
 * 
 * 		(1)		nickname 							//only nickname of
 * 
 * 
 * conversation structure (filename = conversation id)
 * 
 * 		(1)		user1:user2 						//both conversation participants
 * 		(2)		id:timestamp:sender:isFile:content	//format of a message's data (if isFile() then content = null)
 * 		(3)			... following messages
 * 
 * 
 * database folder structure
 * 		SiNaS 	/ conversations / (conversation files called <id>.txt)
 * 				/ files / (files called <id>.*)
 * 				/ users / (userdata files called <username>.txt)
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
			String var;
			while ((var = reader.readLine()) != null) {
				lines.add(var);
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
				String var;
				while ((var = reader.readLine()) != null) {
					lines.add(var);
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
				String var;
				while ((var = reader.readLine()) != null) {
					lines.add(var);
				}
				reader.close();
				String[] conversationInformation = lines.get(0).split(":");
				if (conversationInformation[0].equals(user.getUsername()) || conversationInformation[1].equals(user.getUsername())) {
					conversations.add(new Conversation(filelist[i].getName(), getUserInfo(conversationInformation[0]).getUsername(), getUserInfo(conversationInformation[1]).getUsername()));
					for(int j = 1; j < lines.size(); j++) {
						String id = lines.get(j).split(":")[0];
						long timestamp = Long.parseLong(lines.get(j).split(":")[1]);
						String sender = lines.get(j).split(":")[2];
						boolean isFile = Boolean.parseBoolean(lines.get(j).split(":")[3]);
						String content = "";
						if(isFile) {
							content = null;
							//TODO file request?
						} else {
							for(int k = 4; k < lines.get(j).split(":").length; k++) {
								content = content + lines.get(j).split(":")[k] + ":";
							}
						}
						conversations.get(j).addMessages(new Message(id, content, timestamp, sender, isFile));
					}
				}
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
