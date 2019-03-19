package de.sinas.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.Users;
import de.sinas.crypto.Encoder;
import de.sinas.crypto.HashHandler;
import de.sinas.crypto.SaltGenerator;
import de.sinas.net.PROTOCOL;
import de.sinas.net.CryptoClient;

public class AppClient extends CryptoClient {
	private final ArrayList<Conversation> conversations = new ArrayList<>();
	private final Users users = new Users();
	private User thisUser = new User("", 0, "", "");
	private boolean isLoggedIn;
	private final HashHandler hashHandler = new HashHandler();

	private final ArrayList<UpdateListener> updateListeners = new ArrayList<>();
	private final ArrayList<ErrorListener> errorListeners = new ArrayList<>();
	private final ConnectionListener connectionListener;
	private SearchResultListener searchResultListener;

	public AppClient(String pServerIP, int pServerPort, ConnectionListener connectionListener) {
		super(pServerIP, pServerPort);
		this.connectionListener = connectionListener;
	}

	@Override
	protected void processDecryptedMessage(String message) {
		String[] msgParts = message.split(PROTOCOL.SPLIT, -1);
		switch (msgParts[0]) {
		case PROTOCOL.SC.LOGIN_OK:
			handleLoginOk();
			break;
		case PROTOCOL.SC.ERROR:
			handleError(msgParts[1]);
			return;
		case PROTOCOL.SC.CONVERSATION:
			handleConversation(msgParts);
			break;
		case PROTOCOL.SC.USER:
			handleUser(msgParts);
			break;
		case PROTOCOL.SC.MESSAGES:
			handleMessages(msgParts);
			break;
		case PROTOCOL.SC.USER_SEARCH_RESULT:
			handleUserSearchResult(msgParts);
			return;
		default:
			break;
		}

		for (UpdateListener updateListener : updateListeners) {
			updateListener.update(msgParts[0]);
		}
	}

	@Override
	protected void connectionLost() {

	}

	@Override
	protected void processSecureConnected() {
		connectionListener.connected(this);
	}

	private void handleLoginOk() {
		isLoggedIn = true;
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
			break;

		default:
			break;
		}
		for (ErrorListener errorListener : errorListeners) {
			errorListener.error(errorCode);
		}
	}

	private void handleConversation(String[] msgParts) {
		String conversationName = msgParts[1];
		String conversationId = msgParts[2];
		String conversationKey = msgParts[3];
		String[] usernames = Arrays.copyOfRange(msgParts, 4, msgParts.length);
		boolean inConversation = false;
		for(String name : usernames) {
			if(name.equals(thisUser.getUsername())) {
				inConversation = true;
			}
		}
		if(!inConversation) {
			Conversation con = null;
			for(Conversation c : conversations) {
				if(c.getId().equals(conversationId)) {
					con = c;
				}
			}
			if(con != null) {
				conversations.remove(con);
			}
			return;
		} 
		addConversationKey(conversationId, conversationKey);
		int conversationIndex = -1;
		for (int i = 0; i < conversations.size(); i++) {
			Conversation c = conversations.get(i);
			if (c.getId().equals(conversationId)) {
				conversationIndex = i;
				break;
			}
		}
		if (conversationIndex == -1) {
			conversations.add(new Conversation(conversationId, conversationName, usernames));
			return;
		}
		Conversation newConversation = new Conversation(conversationId, conversationName, usernames);
		newConversation.addMessages(conversations.get(conversationIndex).getMessages().toArray(new Message[0]));
		conversations.set(conversationIndex, newConversation);
	}

	private void handleUser(String[] msgParts) {
		if (users.doesUserExist(msgParts[1])) {
			users.removeUser(users.getUser(msgParts[1]));
		}
		users.addUser(new User("", 0, msgParts[1], ""));
	}

	private void handleMessages(String[] msgParts) {
		String conversationId = msgParts[1];
		Conversation conversation = null;
		for (Conversation con : conversations) {
			if (con.getId().equals(conversationId)) {
				conversation = con;
				break;
			}
		}
		if (conversation == null) {
			return;
		}
		Message[] messages = new Message[msgParts.length / 5];
		for (int i = 0; i < messages.length; i++) {
			String messageId = msgParts[i * 5 + 2];
			boolean isFile = Boolean.parseBoolean(msgParts[i * 5 + 3]);
			long timestamp;
			try {
				timestamp = Long.parseLong(msgParts[i * 5 + 4]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return;
			}
			String sender = msgParts[i * 5 + 5];
			String content = msgParts[i * 5 + 6];
			messages[i] = new Message(messageId, content, timestamp, sender, isFile, conversationId);
		}
		conversation.addMessages(messages);
	}

	private void handleUserSearchResult(String[] msgParts) {
		String query = msgParts.length >= 2 ? msgParts[1] : "";
		String[] results = msgParts.length >= 3 ? Arrays.copyOfRange(msgParts, 2, msgParts.length) : new String[0];
		if (searchResultListener != null) {
			searchResultListener.searchResult(query, results);
		}
	}

	public void addUpdateListener(UpdateListener updateListener) {
		updateListeners.add(updateListener);
	}

	public void removeAllUpdateListeners() {
		updateListeners.clear();
	}

	public void addErrorListener(ErrorListener errorListener) {
		errorListeners.add(errorListener);
	}

	public void removeAllErrorListeners() {
		errorListeners.clear();
	}

	public void setSearchResultListener(SearchResultListener searchResultListener) {
		this.searchResultListener = searchResultListener;
	}

	public void removeSearchResultListener() {
		searchResultListener = null;
	}

	public void login(String username, String password) {
		String pwdHash = Encoder.b64Encode(hashHandler.getSecureHash(password, SaltGenerator.generateSalt(username, password, hashHandler)));
		thisUser = new User("", 0, username, pwdHash);
		send(PROTOCOL.CS.LOGIN, thisUser.getUsername(), thisUser.getPasswordHash());
	}

	public void register(String username, String password) {
		String pwdHash = Encoder.b64Encode(hashHandler.getSecureHash(password, SaltGenerator.generateSalt(username, password, hashHandler)));
		thisUser = new User("", 0, username, pwdHash);
		send(PROTOCOL.CS.REGISTER, thisUser.getUsername(), thisUser.getPasswordHash());
	}

	public void requestConversations() {
		send(PROTOCOL.CS.GET_CONVERSATIONS);
	}

	public void requestMessages(String convId, long lastTimestamp, int amount) {
		send(PROTOCOL.CS.GET_MESSAGES, convId, lastTimestamp, amount);
	}

	public void addConversation(String name) {
		send(PROTOCOL.CS.CREATE_CONVERSATION, name, thisUser.getUsername());
	}

	public void addUserToConversation(String convId, String user) {
		send(PROTOCOL.CS.CONVERSATION_ADD, convId, user);
	}

	public void removeUserFromConversation(String convId, String user) {
		send(PROTOCOL.CS.CONVERSATION_REM, convId, user);
	}

	public void renameConversation(String convId, String newName) {
		send(PROTOCOL.CS.CONVERSATION_RENAME, convId, newName);
	}

	public void searchUser(String query) {
		send(PROTOCOL.CS.USER_SEARCH, query);
	}

	public User getThisUser() {
		return thisUser;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public ArrayList<Conversation> getConversations() {
		return conversations;
	}

	@FunctionalInterface
	public interface UpdateListener extends EventListener {
		void update(String msgBase);
	}

	@FunctionalInterface
	public interface ErrorListener extends EventListener {
		void error(int errorCode);
	}

	@FunctionalInterface
	public interface ConnectionListener extends EventListener {
		void connected(AppClient appClient);
	}

	@FunctionalInterface
	public interface SearchResultListener extends EventListener {
		void searchResult(String query, String[] results);
	}
}
