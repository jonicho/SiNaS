package de.sinas.server;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.crypto.Encoder;
import de.sinas.net.PROTOCOL;
import de.sinas.net.Server;

import javax.crypto.SecretKey;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppServer extends Server {
	private final Database db;
	private final Users users = new Users();
	private final ArrayList<Conversation> conversations = new ArrayList<>();
	private final CryptoSessionManager cryptoManager = new CryptoSessionManager();
	private final ArrayList<TempUser> tempUsers = new ArrayList<TempUser>();
	private final ConversationCryptoManager convCryptoManager = new ConversationCryptoManager();

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
	public void processMessage(String clientIP, int clientPort, String message) {
		System.out.println("(SERVER)New message: " + clientIP + ":" + clientPort + ", " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		User user = users.getUser(clientIP, clientPort);
		if (user != null && cryptoManager.getSessionByUser(user) != null) {
			SecretKey key;
			String encodedMessage;
			if (msgParts.length == 1) {
				key = cryptoManager.getSessionByUser(user).getMainAESKey();
				encodedMessage = msgParts[0];
			} else {
				key = convCryptoManager.getSession(user, getConversationById(msgParts[0])).getAesKey();
				encodedMessage = msgParts[1];
			}
			msgParts = new String(getAesHandler().decrypt(Encoder.b64Decode(encodedMessage), key)).split(PROTOCOL.SPLIT);
		}
		if (user == null) {
			if (msgParts[0].equals(PROTOCOL.CS.CREATE_SEC_CONNECTION)) {
				handleCreateSecConnection(new TempUser(clientIP, clientPort), msgParts);
			} else {
				if (msgParts.length > 1) {
					sendError(new TempUser(clientIP, clientPort), PROTOCOL.ERRORCODES.INVALID_MESSAGE);
					return;
				}
				TempUser tempUser = null;
				for (TempUser tu : tempUsers) {
					if (tu.getIp().equals(clientIP) && tu.getPort() == clientPort) {
						tempUser = tu;
						break;
					}
				}
				if (tempUser == null) {
					sendError(new TempUser(clientIP, clientPort), PROTOCOL.ERRORCODES.NOT_SEC_CONNECTED);
					return;
				}
				msgParts = new String(getAesHandler().decrypt(Encoder.b64Decode(msgParts[0]), tempUser.getAesKey()))
						.split(PROTOCOL.SPLIT);
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
			default:
				sendError(user, PROTOCOL.ERRORCODES.UNKNOWN_MESSAGE_BASE);
				break;
		}
	}

	private void handleCreateSecConnection(TempUser tUser, String[] msgParts) {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Encoder.b64Decode(msgParts[1]));
		try {
			KeyFactory keyFact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = keyFact.generatePublic(keySpec);
			tUser.setRsaKey(pubKey);
			tUser.setAesKey(getAesHandler().generateKey());
			tempUsers.add(tUser);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		sendRSA(new User(tUser.getIp(), tUser.getPort(), "", ""), tUser.getRsaKey(), PROTOCOL.buildMessage(PROTOCOL.SC.SEC_CONNECTION_ACCEPTED, Encoder.b64Encode(tUser.getAesKey().getEncoded())));
	}

	private void handleRegister(TempUser tUser, String username, String password) {
		if (db.loadConnectedUser(username, tUser.getIp(), tUser.getPort()) instanceof TempUser) {
			db.createUser(new User(tUser.getIp(), tUser.getPort(), username, password));
			handleLogin(tUser, username, password);
		} else {
			send(tUser.getIp(), tUser.getPort(), PROTOCOL.buildMessage(PROTOCOL.SC.ERROR, PROTOCOL.ERRORCODES.ALREADY_REGISTERED));
			handleLogin(tUser, username, password);
		}
	}

	/**
	 * Handles a login request.<br>
	 * Determines the username with the owner attribute of the file with the ip as
	 * filename in the SiNaS login directory.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleLogin(TempUser tUser, String username, String password) {
		User user = db.loadConnectedUser(username, tUser.getIp(), tUser.getPort());
		if (!(user instanceof TempUser) && user.getPasswordHash().equals(password)) {
			tempUsers.remove(tUser);
			cryptoManager.addSession(new CryptoSession(user, tUser.getRsaKey(), tUser.getAesKey()));

			// load all conversations of this user and add them if they are not in the
			// conversation list yet
			for (Conversation conversation : db.loadConversations(user)) {
				if (!conversations.contains(conversation)) {
					conversations.add(conversation);
				}
			}
			users.addUser(user);
			sendAES(user, PROTOCOL.SC.LOGIN_OK, user.getUsername());
		} else {
			sendError(user, PROTOCOL.ERRORCODES.LOGIN_FAILED);
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
				StringBuilder usersString = new StringBuilder(conversation.getUsers().get(0));
				for (int i = 1; i < conversation.getUsers().size(); i++) {
					usersString.append(PROTOCOL.SPLIT).append(conversation.getUsers().get(i));
				}
				if (!convCryptoManager.hasSession(user, conversation)) {
					ConversationCryptoSession ccs = new ConversationCryptoSession(conversation, user);
					ccs.setAesKey(getAesHandler().generateKey());
					convCryptoManager.addSession(ccs);
				}
				sendAES(user, PROTOCOL.SC.CONVERSATION, conversation.getName(), conversation.getId(), convCryptoManager.getSession(user, conversation).getAesKey().getEncoded(), usersString.toString());
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
		sendAES(requestingUser, PROTOCOL.SC.USER, user.getUsername());
	}

	/**
	 * Handles a get messages request.<br>
	 * Sends the last {@code amount} messages of the requested conversation.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleGetMessages(User user, String[] msgParts) {
		if (msgParts.length < 3) {
			sendError(user, PROTOCOL.ERRORCODES.INVALID_MESSAGE);
			return;
		}
		String conversationId = msgParts[1];
		int lastNMessages = 0;
		try {
			lastNMessages = Integer.parseInt(msgParts[2]);
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
		List<Message> messages = conversation.getMessages();
		for (int i = 0; i < lastNMessages; i++) {
			int index = messages.size() - 1 - i;
			if (index < 0) {
				break;
			}
			Message msg = messages.get(index);
			sendAES(user, PROTOCOL.SC.MESSAGE, conversationId, msg.getId(), msg.isFile(), msg.getTimestamp(),
					msg.getSender(), msg.getContent());
		}
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
		String content = msgParts[3];
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
			sendToUser(user, PROTOCOL.SC.ERROR, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
			return;
		}
		conv.addMessages(message);
		db.createMessage(message);
		sendToConversationAES(conv, PROTOCOL.SC.MESSAGE, conv.getId(), message.getId(), message.isFile(),
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
		StringBuilder usersString = new StringBuilder(newConversation.getUsers().get(0));
		for (int i = 1; i < newConversation.getUsers().size(); i++) {
			usersString.append(PROTOCOL.SPLIT).append(newConversation.getUsers().get(i));
		}
		sendToConversationAES(newConversation, PROTOCOL.SC.CONVERSATION, newConversation.getId(),
				newConversation.getName(), usersString.toString());
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
		conversation.addUser(msgParts[2]);
		db.addUserToConversation(conversation, msgParts[2]);
		StringBuilder usersString = new StringBuilder(conversation.getUsers().get(0));
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString.append(PROTOCOL.SPLIT).append(conversation.getUsers().get(i));
		}
		sendToConversationAES(conversation, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(),
				usersString.toString());
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
		String usersString = conversation.getUsers().get(0);
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString += PROTOCOL.SPLIT + conversation.getUsers().get(i);
		}
		sendToConversationAES(conversation, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(),
				usersString);
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
		StringBuilder usersString = new StringBuilder(conversation.getUsers().get(0));
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString.append(PROTOCOL.SPLIT).append(conversation.getUsers().get(i));
		}
		sendToConversationAES(conversation, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(),
				usersString.toString());
	}

	/**
	 * Sends the given message to the given user.
	 */
	private void sendToUser(User user, Object... message) {
		send(user.getIp(), user.getPort(), PROTOCOL.buildMessage(message));
	}

	/**
	 * Sends the given message to the given user.
	 * The message is encrypted using the given key and the AES Algorithm
	 */
	private void sendAES(User user, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = getAesHandler().encrypt(msg.getBytes(), cryptoManager.getSessionByUser(user).getMainAESKey());
		String enc = Encoder.b64Encode(cryp);
		send(user.getIp(), user.getPort(), enc);
	}

	/**
	 * Sends the given message to the given user.
	 * The message is encrypted using the given key and the RSA Algorithm
	 */
	private void sendRSA(User user, PublicKey key, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = getRsaHandler().encrypt(msg.getBytes(), key);
		String enc = Encoder.b64Encode(cryp);
		send(user.getIp(), user.getPort(), enc);
	}

	private void sendToConversationAES(Conversation con, Object... message) {
		for (String username : con.getUsers()) {
			User user = users.getUser(username);
			if (user != null && convCryptoManager.hasSession(user, con)) {
				ConversationCryptoSession ccs = convCryptoManager.getSession(user, con);
				String msg = PROTOCOL.buildMessage(message);
				byte[] cryp = getAesHandler().encrypt(msg.getBytes(), ccs.getAesKey());
				String enc = Encoder.b64Encode(cryp);
				send(user.getIp(), user.getPort(), ccs.getConv().getId() + PROTOCOL.SPLIT + enc);
			}
		}

	}

	/**
	 * Sends the given error code to the given user.
	 */
	private void sendError(User user, int errorCode) {
		sendToUser(user, PROTOCOL.getErrorMessage(errorCode));
	}

	/**
	 * Returns the conversation with the given id.
	 *
	 * @return the conversation with the given id. {@code null} if there is no such
	 * conversations.
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
