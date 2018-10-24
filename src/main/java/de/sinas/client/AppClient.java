package de.sinas.client;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.crypto.Encoder;
import de.sinas.net.Client;
import de.sinas.net.PROTOCOL;
import de.sinas.server.Users;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class AppClient extends Client {
	private final ArrayList<Conversation> conversations = new ArrayList<>();
	private final Users users = new Users();
	private User thisUser = new User("", 0, "", "");
	private boolean isLoggedIn;
	private boolean isRSA;
	private SecretKey rsaPrivKey;
	private SecretKey rsaPubKey;
	private SecretKey mainAESKey;
	private ArrayList<ClientCryptoConversation> cryptoSessions = new ArrayList<ClientCryptoConversation>();

	public AppClient(String pServerIP, int pServerPort) {
		super(pServerIP, pServerPort);
		makeConnection();
	}

	@Override
	public void processMessage(String message) {
		System.out.println("New message: " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		if(isRSA) {
			String dec = new String(Encoder.b64Decode(msgParts[0]));
			String plainText = new String(this.gethRSA().decrypt(dec.getBytes(), rsaPrivKey));
			msgParts = plainText.split(PROTOCOL.SPLIT);
			isRSA = false;
		} else {
			if(msgParts.length == 1) {
				String dec = new String(Encoder.b64Decode(msgParts[0]));
				String plainText = new String(this.gethAES().decrypt(dec.getBytes(), mainAESKey));
				msgParts = plainText.split(PROTOCOL.SPLIT);
			} else {
				String dec = new String(Encoder.b64Decode(msgParts[0]));
				SecretKey cKey = null;
				for(ClientCryptoConversation ccc : cryptoSessions) {
					if(ccc.getConversationID().equals(msgParts[0])) {
						cKey = ccc.getAesKey();
					}
				}
				String plainText = new String(this.gethAES().decrypt(dec.getBytes(), cKey));
				msgParts = plainText.split(PROTOCOL.SPLIT);
			}
		}
		switch (msgParts[0]) {
			case PROTOCOL.SC.LOGIN_OK:
				handleLoginOk();
				break;
			case PROTOCOL.SC.ERROR:
				handleError(msgParts[1]);
				break;
				case PROTOCOL.SC.SEC_CONNECTION_ACCEPTED:
				handleSecConAccept(msgParts);
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

	private void handleSecConAccept(String[] msgParts) {
		mainAESKey = new SecretKeySpec(msgParts[1].getBytes(),"AES");
	} 

	private void handleLoginOk() {
		isLoggedIn = true;
		sendAES(PROTOCOL.buildMessage(PROTOCOL.CS.GET_CONVERSATIONS));
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
	}

	private void handleConversation(String[] msgParts) {
		String conversationName = msgParts[1];
		String conversationId = msgParts[2];
		String convesationKey = msgParts[3];
		String[] usernames = Arrays.copyOfRange(msgParts, 3, msgParts.length);
		SecretKey conKey = new SecretKeySpec(convesationKey.getBytes(),"AES");
		cryptoSessions.add(new ClientCryptoConversation(conKey,conversationId));
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

	private void handleMessage(String[] msgParts) {
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
		String messageId = msgParts[2];
		boolean isFile = Boolean.parseBoolean(msgParts[3]);
		long timestamp;
		try {
			timestamp = Long.parseLong(msgParts[4]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		String sender = msgParts[5];
		String content = msgParts[6];
		conversation.addMessages(new Message(messageId, content, timestamp, sender, isFile, conversationId));
	}

	private void makeConnection() {
		KeyPair kp = this.gethRSA().generateKeyPair();
		rsaPrivKey = new SecretKeySpec(kp.getPrivate().getEncoded(),"RSA");
		rsaPubKey = new SecretKeySpec(kp.getPublic().getEncoded(),"RSA");
		isRSA = true;
		send(PROTOCOL.buildMessage(PROTOCOL.CS.CREATE_SEC_CONNECTION, new String(rsaPubKey.getEncoded())));
	}

	public void login(String username, String passwordHash) {
		thisUser = new User("", 0, username, passwordHash);
		sendAES(PROTOCOL.buildMessage(PROTOCOL.CS.LOGIN, thisUser.getUsername(), thisUser.getPasswordHash()));
	}

	public void register(String username, String passwordHash) {
		thisUser = new User("", 0, username, passwordHash);
		sendAES(PROTOCOL.buildMessage(PROTOCOL.CS.REGISTER, thisUser.getUsername(), thisUser.getPasswordHash()));
	}

	private void sendMessage(String convID, String content) {
		Conversation cCon = null;
		for(Conversation con : conversations) {
			if(con.getId().equals(convID)) {
				cCon = con;
			}
		}
		ClientCryptoConversation ccc = null;
		for(ClientCryptoConversation pccc : cryptoSessions) {
			if(pccc.getConversationID().equals(convID)) {
				ccc = pccc;
			}
		}
		content = false + PROTOCOL.SPLIT + content;
		byte[] cryp = super.gethAES().encrypt(content.getBytes(), ccc.getAesKey());
		String enc = Encoder.b64Encode(cryp);
		send(convID+PROTOCOL.SPLIT+enc);
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

	/**
	 * Sends the given message to the given user.
	 * The message is encrypted using the given key and the AES Algorithm
	 */
	private void sendAES(Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = super.gethAES().encrypt(msg.getBytes(), mainAESKey);
		String enc = Encoder.b64Encode(cryp);
		send(enc);
	}

	/**
	 * Sends the given message to the given user.
	 * The message is encrypted using the given key and the RSA Algorithm
	 */
	private void sendRSA(SecretKey key, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = super.gethRSA().encrypt(msg.getBytes(), key);
		String enc = Encoder.b64Encode(cryp);
		send(enc);
	}
}
