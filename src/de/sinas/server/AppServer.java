package de.sinas.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

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
			handleGetConversations(user, msgParts);
			break;
		case PROTOCOL.CS.GET_USER:
			handleGetUser(user, msgParts);
			break;
		case PROTOCOL.CS.GET_MESSAGES:
			handleGetMessages(user, msgParts);
			break;
		default:
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

	private void handleGetConversations(User user, String[] msgParts) {
		for (Conversation conversation : conversations) {
			if (conversation.contains(user.getUsername())) {
				sendToUser(user, PROTOCOL.SC.CONVERSATION, conversation.getId(), "false",
						conversation.getUsers().get(0), conversation.getUsers().get(1));// TODO: handle group
																						// conversations
			}
		}
	}

	private void handleGetUser(User requestingUser, String[] msgParts) {
		User user = users.getUser(msgParts[1]);
		if (user == null) {
			user = db.getUserInfo(msgParts[1]);
			if (user == null) {
				sendError(requestingUser, PROTOCOL.ERRORCODES.USER_DOES_NOT_EXIST);
			}
		}
		sendToUser(requestingUser, PROTOCOL.SC.USER, user.getUsername(), user.getNickname());
	}

	private void handleGetMessages(User user, String[] msgParts) {
		// TODO handle group conversations
		String conversationId = msgParts[1];
		int lastNMessages = 0;
		try {
			lastNMessages = Integer.parseInt(msgParts[2]);
		} catch (NumberFormatException e) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
		}
		Conversation conversation = null;
		for (Conversation c : conversations) {
			if (c.getId().equals(conversationId)) { // TODO check whether the conversation contains the requesting user
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
					msg.getContent());
		}
	}

	private void handleMessage(User user, String[] msgParts) {
		long ms = System.currentTimeMillis();
		Conversation conv = null;
		String convID = msgParts[1];
		for (Conversation c : conversations) {
			if (c.getId().equals(convID)) {
				conv = c;
			}
		}
		boolean isFile = Boolean.parseBoolean(msgParts[2]);
		String hString = msgParts[3] + ms;
		String idString = "";
		try {
			byte[] stringBytes = hString.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hashBytes = md.digest(stringBytes);
			byte[] encodedBytes = Base64.getEncoder().encode(hashBytes);
			idString = new String(encodedBytes);
		} catch (Exception ex) {
			ex.printStackTrace();
			sendToUser(user, PROTOCOL.SC.ERROR, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
			return;
		}
		Message cMessage = new Message(idString, msgParts[3], ms, user.getUsername(), isFile);
		conv.addMessages(cMessage);
		sendToUser(users.getUser(conv.getUsers().get(0)), PROTOCOL.SC.MESSAGE, conv.getId(), cMessage.getId(),
				cMessage.isFile(), cMessage.getTimestamp(), cMessage.getContent());
		sendToUser(users.getUser(conv.getUsers().get(1)), PROTOCOL.SC.MESSAGE, conv.getId(), cMessage.getId(),
				cMessage.isFile(), cMessage.getTimestamp(), cMessage.getContent());
	}

	private void sendToUser(User user, Object... message) {
		send(user.getIp(), user.getPort(), PROTOCOL.buildMessage(message));
	}

	private void sendError(User user, int errorCode) {
		sendToUser(user, PROTOCOL.getErrorMessage(errorCode));
	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
	}
}
