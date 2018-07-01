package de.sinas.server;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.net.PROTOCOL;
import de.sinas.net.Server;

public class AppServer extends Server {
	private final Database db;
	private final Users users = new Users();
	private final ArrayList<Conversation> conversations = new ArrayList<>();
	private final File loginDirectory;

	public AppServer(int pPort, File databaseDirectory, File loginDirectory) {
		super(pPort);
		db = new Database(databaseDirectory);
		this.loginDirectory = loginDirectory;
	}

	@Override
	public void processNewConnection(String clientIP, int clientPort) {
		System.out.println("New connection: " + clientIP + ":" + clientPort);
	}

	@Override
	public void processMessage(String clientIP, int clientPort, String message) {
		System.out.println("New message: " + clientIP + ":" + clientPort + ", " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		User user = users.getUser(clientIP, clientPort);
		if (user == null) {
			if (!msgParts[0].equals(PROTOCOL.CS.LOGIN)) {
				send(clientIP, clientPort, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.NOT_LOGGED_IN));
			} else {
				handleLogin(clientIP, clientPort);
			}
			return;
		}
		switch (msgParts[0]) {
		case PROTOCOL.CS.MESSAGE:
			handleMessage(user, msgParts);
			break;
		case PROTOCOL.CS.GET_CONVERSATIONS:
			handleGetConversations(user);
			break;
		case PROTOCOL.CS.GET_USER:
			handleGetUser(user, msgParts);
			break;
		case PROTOCOL.CS.GET_MESSAGES:
			handleGetMessages(user, msgParts);
			break;
		case PROTOCOL.CS.CREATE_CONVERSATION:
			handleCreateConversation(user, msgParts);
			break;
		default:
			sendError(user, PROTOCOL.ERRORCODES.UNKNOWN_MESSAGE_BASE);
			break;
		}
	}

	private void handleLogin(String clientIP, int clientPort) {
		Path path = Paths.get(loginDirectory.getAbsolutePath() + "/" + clientIP);
		FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
		try {
			String[] ownerName = ownerAttributeView.getOwner().getName().split("\\\\");
			User user = db.getConnectedUser(ownerName[ownerName.length - 1], clientIP, clientPort);
			// load all conversations of this user and add them if they are not in the
			// conversation list yet
			for (Conversation conversation : db.getConversations(user)) {
				if (!conversations.contains(conversation)) {
					conversations.add(conversation);
				}
			}
			users.addUser(user);
			sendToUser(user, PROTOCOL.SC.LOGIN_OK, user.getUsername(), user.getNickname());
		} catch (IOException e) {
			e.printStackTrace();
			send(clientIP, clientPort, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.LOGIN_FAILED));
		}

	}

	private void handleGetConversations(User user) {
		for (Conversation conversation : conversations) {
			if (conversation.contains(user.getUsername())) {
				String usersString = conversation.getUsers().get(0);
				for (int i = 1; i < conversation.getUsers().size(); i++) {
					usersString += PROTOCOL.SPLIT + conversation.getUsers().get(i);
				}
				sendToUser(user, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(), usersString);
			}
		}
	}

	private void handleGetUser(User requestingUser, String[] msgParts) {
		if (msgParts.length < 2) {
			sendError(requestingUser, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
		}
		User user = users.getUser(msgParts[1]);
		if (user == null) {
			user = db.getUserInfo(msgParts[1]);
			if (user == null) {
				sendError(requestingUser, PROTOCOL.ERRORCODES.USER_DOES_NOT_EXIST);
				return;
			}
		}
		sendToUser(requestingUser, PROTOCOL.SC.USER, user.getUsername(), user.getNickname());
	}

	private void handleGetMessages(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
		}
		String conversationId = msgParts[1];
		int lastNMessages = 0;
		try {
			lastNMessages = Integer.parseInt(msgParts[2]);
		} catch (NumberFormatException e) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
		}
		Conversation conversation = null;
		for (Conversation c : conversations) {
			if (c.getId().equals(conversationId)) {
				if (!c.contains(user.getUsername())) {
					sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
					return;
				}
				conversation = c;
				break;
			}
		}
		if (conversation == null) {
			sendError(user, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
			return;
		}
		List<Message> messages = conversation.getMessages();
		for (int i = 0; i < lastNMessages; i++) {
			int index = messages.size() - 1 - i;
			if (index < 0) {
				break;
			}
			Message msg = messages.get(index);
			sendToUser(user, PROTOCOL.SC.MESSAGE, conversationId, msg.getId(), msg.isFile(), msg.getTimestamp(),
					msg.getSender(), msg.getContent());
		}
	}

	private void handleMessage(User user, String[] msgParts) {
		if (msgParts.length < 4) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
		}
		long ms = System.currentTimeMillis();
		Conversation conv = null;
		String convID = msgParts[1];
		for (Conversation c : conversations) {
			if (c.getId().equals(convID)) {
				conv = c;
			}
		}
		boolean isFile = Boolean.parseBoolean(msgParts[2]);
		String content = msgParts[3];
		Message message;
		try {
			message = new Message(content, ms, user.getUsername(), isFile);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			sendToUser(user, PROTOCOL.SC.ERROR, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
			return;
		}
		conv.addMessages(message);
		db.saveConversation(conv);
		sendToConversation(conv, PROTOCOL.SC.MESSAGE, conv.getId(), message.getId(), message.isFile(),
				message.getTimestamp(), message.getSender(), message.getContent());
	}

	private void handleCreateConversation(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
		}
		String name = msgParts[1];
		String[] users = Arrays.copyOfRange(msgParts, 2, msgParts.length);
		if (!Arrays.asList(users).contains(user.getUsername())) {
			sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
			return;
		}
		for (String u : users) {
			if (db.getUserInfo(u) == null) {
				sendError(user, PROTOCOL.ERRORCODES.USER_DOES_NOT_EXIST);
				return;
			}
		}
		Conversation newConversation = new Conversation(name, users);
		conversations.add(newConversation);
		db.saveConversation(newConversation);
		String usersString = newConversation.getUsers().get(0);
		for (int i = 1; i < newConversation.getUsers().size(); i++) {
			usersString += PROTOCOL.SPLIT + newConversation.getUsers().get(i);
		}
		sendToConversation(newConversation, PROTOCOL.SC.CONVERSATION, newConversation.getId(),
				newConversation.getName(), usersString);
	}

	private void sendToUser(User user, Object... message) {
		send(user.getIp(), user.getPort(), PROTOCOL.buildMessage(message));
	}

	private void sendToConversation(Conversation conversation, Object... message) {
		for (String username : conversation.getUsers()) {
			User user = users.getUser(username);
			if (user != null) {
				sendToUser(user, message);
			}
		}
	}

	private void sendError(User user, int errorCode) {
		sendToUser(user, PROTOCOL.getErrorMessage(errorCode));
	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
		users.removeUser(users.getUser(pClientIP, pClientPort));
	}
}
