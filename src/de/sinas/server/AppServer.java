package de.sinas.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.util.ArrayList;
import java.util.List;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.net.PROTOCOL;
import de.sinas.net.Server;

public class AppServer extends Server {
	private Database db = new Database(new File("C:\\Users\\jonas.keller\\Desktop\\SiNaS-Database"));
	private Users users = new Users();
	private ArrayList<Conversation> conversations = new ArrayList<>();

	public AppServer(int pPort) {
		super(pPort);
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
		case PROTOCOL.CS.MSG:
			handleMessage(user, msgParts);
			break;
		case PROTOCOL.CS.GET_CONVERSATION_LIST:
			handleGetConversationList(user, msgParts);
			break;
		case PROTOCOL.CS.GET_CONVERSATION:
			handleGetConversation(user, msgParts);
			break;
		default:
			break;
		}
	}

	private void handleLogin(String clientIP, int clientPort) {
		Path path = Paths.get("T:\\Schulweiter Tausch\\" + clientIP);
		FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
		try {
			String[] ownerName = ownerAttributeView.getOwner().getName().split("\\\\");
			User user = db.getUser(ownerName[ownerName.length - 1], clientIP, clientPort);
			for (Conversation conversation : db.getConversations(user)) {
				if (!conversations.contains(conversation)) {
					conversations.add(conversation);
				}
			}
			send(user.getIp(), user.getPort(),
					PROTOCOL.buildMessage(PROTOCOL.SC.LOGIN_OK, user.getUsername(), user.getNickname()));
		} catch (IOException e) {
			e.printStackTrace();
			send(clientIP, clientPort, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.LOGIN_FAILED));
		}

	}

	private void handleGetConversationList(User user, String[] msgParts) {
		String msg = PROTOCOL.SC.CONVERSATION_LIST;
		for (Conversation conversation : conversations) {
			if (conversation.getUser1().equals(user.getUsername())) {
				msg += PROTOCOL.SPLIT + conversation.getId() + PROTOCOL.SPLIT + "false"; // TODO: handle group
																							// conversations
			}
		}
		sendToUser(user, msg);
	}

	private void handleGetConversation(User user, String[] msgParts) {
		String conversationId = msgParts[1];
		int lastNMessages = 0;
		try {
			lastNMessages = Integer.parseInt(msgParts[2]);
		} catch (NumberFormatException e) {
			sendToUser(user, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.INVALID_MESSAGE));
		}
		Conversation conversation = null;
		for (Conversation c : conversations) {
			if (c.getId().equals(conversationId)) { // TODO check whether the conversation contains the requesting user
				conversation = c;
				break;
			}
		}
		if (conversation == null) {
			sendToUser(user, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.UNKNOWN_ERROR));
			return;
		}
		sendToUser(user, PROTOCOL.buildMessage(PROTOCOL.SC.CONVERSATION, conversation.getId(), false,
				conversation.getUser1(), conversation.getUser2())); // TODO handle group conversations
		List<Message> messages = conversation.getMessages();
		for (int i = 0; i < lastNMessages; i++) {
			int index = messages.size() - 1 - i;
			if (index < 0) {
				break;
			}
			Message msg = messages.get(index);
			sendToUser(user, PROTOCOL.buildMessage(PROTOCOL.SC.MSG, conversationId, msg.getId(), msg.isFile(),
					msg.getTimestamp(), msg.getContent()));
		}
	}

	private void handleMessage(User user, String[] msgParts) {

	}

	private void sendToUser(User user, String message) {
		send(user.getIp(), user.getPort(), message);
	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
	}
}
