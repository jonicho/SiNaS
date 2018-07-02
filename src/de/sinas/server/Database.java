package de.sinas.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;

/**
 * The database for SiNaS. Stores user and conversation data in the given
 * directory<br>
 * <br>
 * user file structure (filename = username)<br>
 * (1) nickname<br>
 * <br>
 * conversation structure (filename = conversation id)<br>
 * (1) convname[SPLIT]user1[SPLIT]user2<br>
 * (2) id[SPLIT]timestamp[SPLIT]sender[SPLIT]isFile[SPLIT]content<br>
 * (3) ... following messages<br>
 * <br>
 * database folder structure<br>
 * SiNaS / conversations / (conversation files called <id>)<br>
 * 		 / files / (files called <id>)<br>
 * 		 / users / (userdata files called <username>)<br>
 */
public class Database {
	private static final String SPLIT = "_";

	private File databaseDirectory;

	/**
	 * Creates a new database object
	 * 
	 * @param databaseDirectory the directory in which the data is to be stored
	 * @throws IllegalArgumentException when given database directory is not a
	 *                                  directory
	 */
	public Database(File databaseDirectory) throws IllegalArgumentException {
		databaseDirectory.mkdir();
		File[] structure = { new File(databaseDirectory, "conversations"), new File(databaseDirectory, "files"),
				new File(databaseDirectory, "users") };
		for (File folder : structure) {
			folder.mkdir();
		}
		if (!databaseDirectory.isDirectory()) {
			throw new IllegalArgumentException("Database directory has to be a directory!");
		}
		this.databaseDirectory = databaseDirectory;
	}

	/**
	 * Loads the user with the given name with ip and port.<br>
	 * This method is to get a user that connected to the server.<br>
	 * If no user with the given name exists a new one is created.
	 * 
	 * @return The user with the given username
	 */
	public User getConnectedUser(String username, String ip, int port) {
		String nickname;
		File file = new File(databaseDirectory + "/users/ " + username);

		if (!file.exists()) {
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
	 * Loads the user with the given name with an empty ip and the port 0.<br>
	 * This method is to get information about a user without having the user to
	 * connect to the server.<br>
	 * If no user with the given name exists {@code null} is returned.
	 * 
	 * @return The user with the given username. {@code null} if no user with the
	 *         given name exists.
	 */
	public User getUserInfo(String username) {
		String nickname;
		File file = new File(databaseDirectory + "/users/ " + username);
		if (!file.exists()) {
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
		File convsDirectory = new File(databaseDirectory, "conversations");
		for (File convFile : convsDirectory.listFiles()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(convFile));
				String line = reader.readLine();
				if (line == null) {
					reader.close();
					continue;
				}
				String[] convInfo = line.split(SPLIT);
				boolean convContainsUser = false;
				for (int i = 1; i < convInfo.length; i++) {
					if (convInfo[i].equals(user.getUsername())) {
						convContainsUser = true;
						break;
					}
				}
				if (!convContainsUser) {
					reader.close();
					continue;
				}

				Conversation newConv = new Conversation(convFile.getName(), convInfo[0],
						Arrays.copyOfRange(convInfo, 1, convInfo.length));

				ArrayList<Message> messages = new ArrayList<Message>();
				while ((line = reader.readLine()) != null) {
					String[] msgInfo = line.split(SPLIT);
					String id = msgInfo[0];
					long timestamp = Long.parseLong(msgInfo[1]);
					String sender = msgInfo[2];
					boolean isFile = Boolean.parseBoolean(msgInfo[3]);
					String content = "";
					if (isFile) {
						// TODO handle file
					} else {
						content = msgInfo[4];
						for (int k = 5; k < msgInfo.length; k++) {
							content += SPLIT + msgInfo[k];
						}
					}
					messages.add(new Message(id, content, timestamp, sender, isFile));
				}
				reader.close();
				newConv.addMessages(messages.toArray(new Message[0]));
				conversations.add(newConv);
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
		File file = new File(databaseDirectory + "/users/" + user.getUsername());
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
		File file = new File(databaseDirectory + "/conversations/" + conversation.getId());
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");

			writer.print(conversation.getName());
			for (int i = 0; i < conversation.getUsers().size(); i++) {
				writer.print(SPLIT + conversation.getUsers().get(i));
			}
			writer.println();

			for (Message message : conversation.getMessages()) {
				writer.println(message.getId() + SPLIT + message.getTimestamp() + SPLIT + message.getSender() + SPLIT
						+ message.isFile() + SPLIT + message.getContent());
			}

			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
