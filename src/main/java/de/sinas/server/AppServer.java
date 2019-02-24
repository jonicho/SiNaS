package de.sinas.server;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.crypto.Encoder;
import de.sinas.crypto.HashHandler;
import de.sinas.net.CryptoServer;
import de.sinas.net.PROTOCOL;

public class AppServer extends CryptoServer {
	private final Database db;
	private final ArrayList<Conversation> conversations = new ArrayList<>();
    private final HashHandler hashHandler = new HashHandler();

	private static final int SALT_LENGTH = 128;

	public AppServer(int pPort, String dbPath) {
		super(pPort);
		db = new Database(dbPath, SALT_LENGTH);
	}

	@Override
	public void processNewConnection(String clientIP, int clientPort) {
		System.out.println("New connection: " + clientIP + ":" + clientPort);
	}

	@Override
	public void processDecryptedMessage(User user, String message) {
		String[] msgParts = message.split(PROTOCOL.SPLIT, -1);
		if (!users.doesUserExist(user.getIp(), user.getPort())) {
			if (!(user instanceof TempUser)) {
				sendError(user, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
				return;
			}
			TempUser tempUser = (TempUser) user;
			switch (msgParts[0]) {
				case PROTOCOL.CS.LOGIN:
					handleLogin(tempUser, msgParts[1], msgParts[2]);
					break;
				case PROTOCOL.CS.REGISTER:
					handleRegister(tempUser, msgParts[1], msgParts[2]);
					break;
				default:
					sendError(tempUser, PROTOCOL.ERRORCODES.NOT_LOGGED_IN);
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
		case PROTOCOL.CS.CONVERSATION_ADD:
			handleConversationAddUser(user, msgParts);
			break;
		case PROTOCOL.CS.CONVERSATION_REM:
			handleConversationRemoveUser(user, msgParts);
			break;
		case PROTOCOL.CS.CONVERSATION_RENAME:
			handleConversationRename(user, msgParts);
			break;
		case PROTOCOL.CS.USER_SEARCH:
			handleUserSearch(user, msgParts);
			break;
		default:
			sendError(user, PROTOCOL.ERRORCODES.UNKNOWN_MESSAGE_BASE);
			break;
		}
	}

	private void handleRegister(TempUser tUser, String username, String password) {
		byte[] salt = db.loadSalt();
		String passwordHash = Encoder.b64Encode(hashHandler.getCheckSum((password + Encoder.b64Encode(salt)).getBytes()));
		if (db.loadConnectedUser(username, tUser.getIp(), tUser.getPort()) instanceof TempUser) {
			db.createUser(new User(tUser.getIp(), tUser.getPort(), username, passwordHash));
			handleLogin(tUser, username, password);
		} else {
			send(tUser.getIp(), tUser.getPort(), PROTOCOL.buildMessage(PROTOCOL.SC.ERROR, PROTOCOL.ERRORCODES.ALREADY_REGISTERED));
			handleLogin(tUser, username, password);
		}
	}

	/**
	 * Handles a login request.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleLogin(TempUser tUser, String username, String password) {
		byte[] salt = db.loadSalt();
		String passwordHash = Encoder.b64Encode(hashHandler.getCheckSum((password + Encoder.b64Encode(salt)).getBytes()));
		User user = db.loadConnectedUser(username, tUser.getIp(), tUser.getPort());
		if (!(user instanceof TempUser) && user.getPasswordHash().equals(passwordHash)) {
			if (users.doesUserExist(username)) {
				sendError(user, PROTOCOL.ERRORCODES.ALREADY_LOGGED_IN);
				return;
			}
			addUserKeys(user, tUser.getRsaKey(), tUser.getAesKey());

			// load all conversations of this user and add them if they are not in the
			// conversation list yet
			for (Conversation conversation : db.loadConversations(user)) {
				if (!conversations.contains(conversation)) {
					conversations.add(conversation);
				}
			}
			users.addUser(user);
			send(user, PROTOCOL.SC.LOGIN_OK, user.getUsername());
		} else {
			sendError(tUser, PROTOCOL.ERRORCODES.LOGIN_FAILED);
		}

	}

	/**
	 * Handles a get conversations request.<br>
	 * Sends the id, name and users of all conversations containing the requesting
	 * user.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleGetConversations(User user) {
		for (Conversation conversation : conversations) {
			if (conversation.contains(user.getUsername())) {
				sendConversationToUser(conversation, user);
			}
		}
	}

	/**
	 * Handles a get user request.<br>
	 * If the requested user exists the username and the nickname are sent back. If
	 * not an error is sent back.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleGetUser(User requestingUser, String[] msgParts) {
		if (msgParts.length < 2) {
			sendError(requestingUser, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		User user = users.getUser(msgParts[1]);
		if (user == null) {
			user = db.loadUserInfo(msgParts[1]);
			if (user == null) {
				sendError(requestingUser, PROTOCOL.ERRORCODES.USER_DOES_NOT_EXIST);
				return;
			}
		}
		send(requestingUser, PROTOCOL.SC.USER, user.getUsername());
	}

	/**
	 * Handles a get messages request.<br>
	 * Sends the last {@code amount} messages of the requested conversation.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleGetMessages(User user, String[] msgParts) {
		if (msgParts.length < 4) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		String conversationId = msgParts[1];
		long lastTimestamp = 0;
		int lastNMessages = 0;
		try {
			lastTimestamp = Long.parseLong(msgParts[2]);
			lastNMessages = Integer.parseInt(msgParts[3]);
		} catch (NumberFormatException e) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
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
		db.loadMessages(conversation, lastTimestamp, lastNMessages);
		List<Message> messages = conversation.getMessages();
		String msgsMessage = PROTOCOL.buildMessage(PROTOCOL.SC.MESSAGES, conversationId);
		if (!messages.isEmpty()) {
			int firstIndex = 0;
			int lastIndex = messages.size() - 1;
			while (lastIndex >= 0 && messages.get(lastIndex).getTimestamp() >= lastTimestamp) {
				lastIndex--;
			}
			firstIndex = Math.max(lastIndex - lastNMessages, 0);
			for (int i = firstIndex; i <= lastIndex; i++) {
				Message msg = messages.get(i);
				msgsMessage = PROTOCOL.buildMessage(msgsMessage, msg.getId(), msg.isFile(), msg.getTimestamp(), msg.getSender(), msg.getContent());
			}
		}
		send(user, msgsMessage);
	}

	/**
	 * Handles a message request.<br>
	 * Sends the requested message to the requested conversation. Uses the reception
	 * time as time stamp.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleMessage(User user, String[] msgParts) {
		if (msgParts.length < 4) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		long ms = System.currentTimeMillis();
		String convID = msgParts[1];
		boolean isFile = Boolean.parseBoolean(msgParts[2]);
		String content = msgParts[3].strip();
		if (content.isBlank()) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		Conversation conv = null;
		for (Conversation c : conversations) {
			if (c.getId().equals(convID)) {
				conv = c;
			}
		}
		if (!conv.contains(user.getUsername())) {
			sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
			return;
		}
		Message message;
		try {
			message = new Message(content, ms, user.getUsername(), isFile, conv.getId());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			sendError(user, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
			return;
		}
		conv.addMessages(message);
		db.createMessage(message);
		sendToConversation(conv, PROTOCOL.SC.MESSAGES, conv.getId(), message.getId(), message.isFile(),
				message.getTimestamp(), message.getSender(), message.getContent());
	}

	/**
	 * Handles a create conversation request.<br>
	 * Creates a new conversation with the requested name and participants. Sends
	 * the conversation info to all participants.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleCreateConversation(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		String name = msgParts[1];
		String[] users = Arrays.copyOfRange(msgParts, 2, msgParts.length);
		if (!Arrays.asList(users).contains(user.getUsername())) {
			sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
			return;
		}
		for (String u : users) {
			if (db.loadUserInfo(u) == null) {
				sendError(user, PROTOCOL.ERRORCODES.USER_DOES_NOT_EXIST);
				return;
			}
		}
		Conversation newConversation = new Conversation(name, users);
		conversations.add(newConversation);
		db.createConversation(newConversation);
		for (String username : users) {
			db.addUserToConversation(newConversation, username);
		}
		for (User u : this.users.getLoggedInUsers(newConversation.getUsers())) {
			sendConversationToUser(newConversation, u);
		}
	}

	/**
	 * Handles a conversation add user request.<br>
	 * Adds the requested user to the requested conversation. Sends the conversation
	 * info to all participants.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleConversationAddUser(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		Conversation conversation = getConversationById(msgParts[1]);
		if (!conversation.contains(user.getUsername())) {
			sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
			return;
		}
		if (db.loadUserInfo(msgParts[2]) == null) {
			sendError(user, PROTOCOL.ERRORCODES.USER_DOES_NOT_EXIST);
			return;
		}
		if (conversation.getUsers().contains(msgParts[2])) {
			sendError(user, PROTOCOL.ERRORCODES.USER_ALREADY_IN_CONVERSATION);
			return;
		}
		conversation.addUser(msgParts[2]);
		db.addUserToConversation(conversation, msgParts[2]);
		for (User u : users.getLoggedInUsers(conversation.getUsers())) {
			sendConversationToUser(conversation, u);
		}
	}

	/**
	 * Handles a conversation remove user request.<br>
	 * Removes the requested user from the requested conversation. Sends the
	 * conversation info to all participants.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleConversationRemoveUser(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		Conversation conversation = getConversationById(msgParts[1]);
		if (!conversation.contains(user.getUsername())) {
			sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
			return;
		}
		conversation.removeUser(msgParts[2]);
		db.removeUserFromConversation(conversation, msgParts[2]);
		for (User u : users.getLoggedInUsers(conversation.getUsers())) {
			sendConversationToUser(conversation, u);
		}
	}

	/**
	 * Handles a conversation rename request.<br>
	 * Renames the requested conversation with the requested name. Sends the
	 * conversation info to all participants.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleConversationRename(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		Conversation conversation = getConversationById(msgParts[1]);
		if (!conversation.contains(user.getUsername())) {
			sendError(user, PROTOCOL.ERRORCODES.REQUEST_NOT_ALLOWED);
			return;
		}
		conversation.rename(msgParts[2]);
		db.updateConversation(conversation);
		for (User u : users.getLoggedInUsers(conversation.getUsers())) {
			sendConversationToUser(conversation, u);
		}
	}

	private void handleUserSearch(User user, String[] msgParts) {
		if (msgParts.length < 2) {
			send(user, PROTOCOL.SC.USER_SEARCH_RESULT, "", String.join(PROTOCOL.SPLIT, db.loadAllUsernames().toArray(new String[0])));
			return;
		}
		String[] results = db.loadAllUsernames().stream().filter(username -> {
			int index = -1;
			for (int i = 0; i < msgParts[1].toCharArray().length; i++) {
				index = username.indexOf(msgParts[1].toCharArray()[i], index + 1);
				if (index < 0) {
					return false;
				}
			}
			return true;
		}).toArray(String[]::new);
		send(user, PROTOCOL.SC.USER_SEARCH_RESULT, msgParts[1], String.join(PROTOCOL.SPLIT, results));
	}

	private void sendConversationToUser(Conversation conversation, User user) {
		StringBuilder usersString = new StringBuilder(conversation.getUsers().get(0));
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString.append(PROTOCOL.SPLIT).append(conversation.getUsers().get(i));
		}
		send(user, PROTOCOL.SC.CONVERSATION, conversation.getName(), conversation.getId(),
				Encoder.b64Encode(getConversationUserKey(user.getIp(), user.getPort(), conversation.getId()).getEncoded()),
				usersString.toString());
	}

	/**
	 * Returns the conversation with the given id.
	 *
	 * @return the conversation with the given id. {@code null} if there is no such
	 *         conversations.
	 */
	private Conversation getConversationById(String id) {
		for (Conversation c : conversations) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		return null;
	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
		users.removeUser(users.getUser(pClientIP, pClientPort));
	}
}
