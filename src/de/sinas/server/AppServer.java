package de.sinas.server;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.crypto.Encoder;
import de.sinas.net.PROTOCOL;
import de.sinas.net.Server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AppServer extends Server {
	private final Database db;
	private final Users users = new Users();
	private final ArrayList<Conversation> conversations = new ArrayList<>();
	private final CryptoSessionManager cryptoManager = new CryptoSessionManager();
	private final ArrayList<TempUser> tempUsers = new ArrayList<TempUser>();
	private final ConversationCryptoManager convCryptoManager = new ConversationCryptoManager();

	public AppServer(int pPort, String dbPath) {
		super(pPort);
		db = new Database(dbPath);
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
		if(user != null && cryptoManager.getSessionByUser(user) != null) {
			if(msgParts.length == 1) {
				byte[] decStr = Encoder.b64Decode(msgParts[0]);
				CryptoSession cs = cryptoManager.getSessionByUser(user);
				String plainText = new String(super.gethAES().decrypt(decStr, cs.getMainAESKey()));
				msgParts = plainText.split(PROTOCOL.SPLIT);
			}
			else {
				Conversation con = getConversationById(msgParts[0]);
				ConversationCryptoSession ccs = convCryptoManager.getSession(user, con);
				byte[] decStr = Encoder.b64Decode(msgParts[1]);
				String plainText = new String(super.gethAES().decrypt(decStr,ccs.getAesKey()));
				msgParts = plainText.split(PROTOCOL.SPLIT);
			}
		}
		if (user == null) {
			if(msgParts[0].equals(PROTOCOL.CS.CREATE_SEC_CONNECTION)) {
				TempUser tUser = new TempUser(clientIP, clientPort);
				SecretKey sKey = new SecretKeySpec(msgParts[1].getBytes(), 0, msgParts[1].length(), "RSA");
				tUser.setRsaKey(sKey);
				tUser.setAesKey(super.gethAES().generateKey());
				tempUsers.add(tUser);
				sendRSA(new User(clientIP,clientPort,"",""), tUser.getRsaKey(), PROTOCOL.buildMessage(PROTOCOL.SC.SEC_CONNECTION_ACCEPTED,tUser.getAesKey()));
			}
			 else {
				for (TempUser tu : tempUsers) {
					if(tu.getIp().equals(clientIP) && tu.getPort() == clientPort) {
						if(msgParts.length > 1) {
							sendError(new User(clientIP,clientPort,"",""),PROTOCOL.ERRORCODES.INVALID_MESSAGE);
						}
						else {
							byte[] decStr = Encoder.b64Decode(msgParts[0]);
							String plainText = new String(super.gethAES().decrypt(decStr, tu.getAesKey()));
							msgParts = plainText.split(PROTOCOL.SPLIT);
							handleLogin(tu,msgParts[1], msgParts[2]);
						}
					}
				}
			}
			return;
		}
		if (msgParts.length < 1) {
			sendError(user, PROTOCOL.ERRORCODES.EMPTY_MESSAGE);
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

	/**
	 * Handles a login request.<br>
	 * Determines the username with the owner attribute of the file with the ip as
	 * filename in the SiNaS login directory.
	 *
	 * @see PROTOCOL.CS
	 */
	private void handleLogin(TempUser tUser, String username, String password) {
		// TODO: handle login
		User user = db.getConnectedUser(username, tUser.getIp(), tUser.getPort());
		if(user.getPassword().equals(password)) {
			tempUsers.remove(tUser);
		CryptoSession cs = new CryptoSession(user);
		cs.setMainAESKey(tUser.getAesKey());
		cs.setUserPublicKey(tUser.getRsaKey());
		cryptoManager.addSession(cs);
		
		// load all conversations of this user and add them if they are not in the
		// conversation list yet
		for (Conversation conversation : db.getConversations(user)) {
			if (!conversations.contains(conversation)) {
				conversations.add(conversation);
			}
		}
		users.addUser(user);
		sendAES(user, PROTOCOL.SC.LOGIN_OK, user.getUsername());
		}
		else {
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
				String usersString = conversation.getUsers().get(0);
				for (int i = 1; i < conversation.getUsers().size(); i++) {
					usersString += PROTOCOL.SPLIT + conversation.getUsers().get(i);
				}
				if(!convCryptoManager.hasSession(user, conversation)) {
					ConversationCryptoSession ccs = new ConversationCryptoSession(conversation,user);
					ccs.setAesKey(super.gethAES().generateKey());
					convCryptoManager.addSession(ccs);
				}
				sendAES(user, PROTOCOL.SC.CONVERSATION,conversation.getName(), conversation.getId(),convCryptoManager.getSession(user, conversation).getAesKey() , usersString);
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
			user = db.getUserInfo(msgParts[1]);
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
		db.saveConversation(conversation);
		String usersString = conversation.getUsers().get(0);
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString += PROTOCOL.SPLIT + conversation.getUsers().get(i);
		}
		sendToConversation(conversation, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(),
				usersString);
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
		db.saveConversation(conversation);
		String usersString = conversation.getUsers().get(0);
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString += PROTOCOL.SPLIT + conversation.getUsers().get(i);
		}
		sendToConversation(conversation, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(),
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
		db.saveConversation(conversation);
		String usersString = conversation.getUsers().get(0);
		for (int i = 1; i < conversation.getUsers().size(); i++) {
			usersString += PROTOCOL.SPLIT + conversation.getUsers().get(i);
		}
		sendToConversation(conversation, PROTOCOL.SC.CONVERSATION, conversation.getId(), conversation.getName(),
				usersString);
	}

	/**
	 * Sends the given message to the given user.
	 */
	private void sendToUser(User user, Object... message) {
		send(user.getIp(), user.getPort(), PROTOCOL.buildMessage(message));
	}

	/*
	* Sends the given message to the given user.
	* The message is encrypted using the given key and the AES Algorithm
	*/
	private void sendAES(User user, SecretKey key, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = super.gethAES().encrypt(msg.getBytes(), key);
		String enc = Encoder.b64Encode(cryp);
		send(user.getIp(),user.getPort(),enc);
	}

	/*
	* Sends the given message to the given user.
	* The message is encrypted using the given key and the AES Algorithm
	*/
	private void sendAES(User user, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = super.gethAES().encrypt(msg.getBytes(), cryptoManager.getSessionByUser(user).getMainAESKey());
		String enc = Encoder.b64Encode(cryp);
		send(user.getIp(),user.getPort(),enc);
	}



	/*
	* Sends the given message to the given user.
	* The message is encrypted using the given key and the RSA Algorithm
	*/
	private void sendRSA(User user, SecretKey key, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = super.gethRSA().encrypt(msg.getBytes(), key);
		String enc = Encoder.b64Encode(cryp);
		send(user.getIp(),user.getPort(),enc);
	}

	/**
	 * Sends the given message to all participants of the given conversation if they
	 * are online.
	 */
	private void sendToConversation(Conversation conversation, Object... message) {
		for (String username : conversation.getUsers()) {
			User user = users.getUser(username);
			if (user != null) {
				sendToUser(user, message);
			}
		}
	}

	private void sendToConversationAES(Conversation con, Object... message) {
		ArrayList<User> destUsers = new ArrayList<User>();
		for (String username : con.getUsers()) {
			User user = users.getUser(username);
			if (user != null && convCryptoManager.hasSession(user, con)) {
				destUsers.add(user);
			}
		}
		for(User u : destUsers) {
			ConversationCryptoSession ccs = convCryptoManager.getSession(u, con);
			String msg = PROTOCOL.buildMessage(message);
			byte[] cryp = super.gethAES().encrypt(msg.getBytes(), ccs.getAesKey());
			String enc = Encoder.b64Encode(cryp);
			send(u.getIp(),u.getPort(),ccs.getConv().getId()+PROTOCOL.SPLIT+enc);
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
