package de.sinas.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.client.gui.Gui;
import de.sinas.net.Client;
import de.sinas.net.PROTOCOL;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppClient extends Client {
	private final Gui gui;
	private final File loginDirectory = new File("T:\\Schulweiter Tausch\\");
	private User thisUser;
	private File authFile;
	private ObservableList<Conversation> conversations = FXCollections.observableArrayList();

	public AppClient(String pServerIP, int pServerPort, Gui gui) {
		super(pServerIP, pServerPort);
		this.gui = gui;
	}

	@Override
	public void processMessage(String message) {
		System.out.println("New message: " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		switch (msgParts[0]) {
		case PROTOCOL.SC.LOGIN_OK:
			handleLoginOk(msgParts[1], msgParts[2]);
			break;
		case PROTOCOL.SC.ERROR:
			handleError(msgParts[1]);
			break;
		case PROTOCOL.SC.CONVERSATION:
			handleConversation(msgParts);
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
		boolean isGroupConversation = Boolean.parseBoolean(msgParts[2]);
		if (isGroupConversation) {
			// TODO handle group conversation
		} else {
			boolean updated = false;
			for (int i = 0; i < conversations.size(); i++) {
				Conversation c = conversations.get(i);
				if (c.getId().equals(conversationId)) {
					Conversation newConversation = new Conversation(conversationId, msgParts[3], msgParts[4]);
					newConversation.addMessages(c.getMessages().toArray(new Message[0]));
					conversations.set(i, newConversation);
					updated = true;
					break;
				}
			}
			if (!updated) {
				conversations.add(new Conversation(conversationId, msgParts[3], msgParts[4]));
			}
		}
	}

	public void login() {
		try {
			File f = new File(loginDirectory.getAbsolutePath() + "\\" + InetAddress.getLocalHost().getHostAddress());
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
