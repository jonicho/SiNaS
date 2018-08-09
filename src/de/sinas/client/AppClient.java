package de.sinas.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Arrays;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.client.gui.Gui;
import de.sinas.net.Client;
import de.sinas.net.PROTOCOL;
import de.sinas.server.Users;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppClient extends Client {
	private final Gui gui;
	private final File loginDirectory = new File("/home/jonas");
	private User thisUser;
	private File authFile;
	private ObservableList<Conversation> conversations = FXCollections.observableArrayList();
	private Users users;
	private String ownIP;
	private int ownPort;

	public AppClient(String pServerIP, int pServerPort, Gui gui) {
		super(pServerIP, pServerPort);
		this.gui = gui;
	}

	@Override
	public void processMessage(String message) {
		System.out.println("New message: " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		switch (msgParts[0]) {
		case PROTOCOL.SC.IP:
			ownIP = msgParts[1];
			ownPort = Integer.parseInt(msgParts[2]);
			login();
			break;
		case PROTOCOL.SC.LOGIN_OK:
			handleLoginOk(msgParts[1], msgParts[2]);
			break;
		case PROTOCOL.SC.ERROR:
			handleError(msgParts[1]);
			break;
		case PROTOCOL.SC.CONVERSATION:
			handleConversation(msgParts);
			break;
		case PROTOCOL.SC.USER:
			handleUser(msgParts);
			break;
		case PROTOCOL.SC.MESSAGE:
			handleMessage(msgParts);
			break;
		default:
			break;
		}

	}

	@Override
	public void connectionLost() {

	}

	private void handleLoginOk(String username, String nickname) {
		if (authFile != null) {
			try {
				Files.delete(authFile.toPath());
			} catch (IOException e) {
			}
			thisUser = new User(null, 0, username, username);
		}
		send(PROTOCOL.buildMessage(PROTOCOL.CS.GET_CONVERSATIONS));
	}

	private void handleError(String error) {
		int errorCode;
		try {
			errorCode = Integer.parseInt(error);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		switch (errorCode) {
		case PROTOCOL.ERRORCODES.LOGIN_FAILED:
			try {
				Files.delete(authFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			thisUser = null;
			break;

		default:
			break;
		}
	}

	private void handleConversation(String[] msgParts) {
		String conversationId = msgParts[1];
		String conversationName = msgParts[2];
		String[] usernames = Arrays.copyOfRange(msgParts, 3, msgParts.length);
		boolean updated = false;
		for (int i = 0; i < conversations.size(); i++) {
			Conversation c = conversations.get(i);
			if (c.getId().equals(conversationId)) {
				Conversation newConversation = new Conversation(conversationId, conversationName, usernames);
				newConversation.addMessages(c.getMessages().toArray(new Message[0]));
				conversations.set(i, newConversation);
				updated = true;
				break;
			}
		}
		if (!updated) {
			conversations.add(new Conversation(conversationId, conversationName, usernames));
		}
	}

	private void handleUser(String[] msgParts) {
		if (users.doesUserExist(msgParts[1])) {
			users.removeUser(users.getUser(msgParts[1]));
		}
		users.addUser(new User("", 0, msgParts[1], msgParts[2]));
	}

	private void handleMessage(String[] msgParts) {
		Conversation conversation = null;
		for (Conversation con : conversations) {
			if (con.getId().equals(msgParts[1])) {
				conversation = con;
				break;
			}
		}
		if (conversation == null) {
			return;
		}
		conversation.addMessages(new Message(msgParts[1], msgParts[6], Long.parseLong(msgParts[4]), msgParts[5],
				Boolean.parseBoolean(msgParts[3])));
	}

	private void login() {
		try {
			File f = new File(loginDirectory, ownIP + " " + ownPort);
			if (f.exists()) {
				Files.delete(f.toPath());
			}
			f.createNewFile();
			authFile = f;
			send(PROTOCOL.buildMessage(PROTOCOL.CS.LOGIN));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public User getThisUser() {
		return thisUser;
	}

	public boolean isLoggedIn() {
		return thisUser != null;
	}

	public ObservableList<Conversation> getConversations() {
		return conversations;
	}

}
