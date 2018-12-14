package de.sinas.client;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.crypto.Encoder;
import de.sinas.net.Client;
import de.sinas.net.PROTOCOL;
import de.sinas.server.Users;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;

public class AppClient extends Client {
	private final ArrayList<Conversation> conversations = new ArrayList<>();
	private final Users users = new Users();
	private User thisUser = new User("", 0, "", "");
	private boolean isLoggedIn;
	private boolean isSecConAccepted;
	private boolean isRSA;
	private PrivateKey rsaPrivKey;
	private PublicKey rsaPubKey;
	private SecretKey mainAESKey;
	private ArrayList<ClientCryptoConversation> cryptoSessions = new ArrayList<ClientCryptoConversation>();

	private final ArrayList<UpdateListener> updateListeners = new ArrayList<>();
	private final ArrayList<ErrorListener> errorListeners = new ArrayList<>();
	private final ConnectionListener connectionListener;

	public AppClient(String pServerIP, int pServerPort, ConnectionListener connectionListener) {
		super(pServerIP, pServerPort);
		this.connectionListener = connectionListener;
		makeConnection();
	}

	@Override
	public void processMessage(String message) {
		System.out.println("(CLIENT) New message: " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		if (isRSA) {
			String plainText = new String(getRsaHandler().decrypt(Encoder.b64Decode(msgParts[0]), rsaPrivKey));
			msgParts = plainText.split(PROTOCOL.SPLIT);
			System.out.println("(CLIENT) Decoded message: " + plainText);
			isRSA = false;
		} else {
			if (msgParts.length == 1) {
				String plainText = new String(getAESHandler().decrypt(Encoder.b64Decode(msgParts[0]), mainAESKey));
				msgParts = plainText.split(PROTOCOL.SPLIT);
			} else if (msgParts.length != 2) {
				SecretKey cKey = null;
				for (ClientCryptoConversation ccc : cryptoSessions) {
					if (ccc.getConversationID().equals(msgParts[0])) {
						cKey = ccc.getAesKey();
					}
				}
				String plainText = new String(getAESHandler().decrypt(Encoder.b64Decode(msgParts[0]), cKey));
				msgParts = plainText.split(PROTOCOL.SPLIT);
			}
		}
		switch (msgParts[0]) {
		case PROTOCOL.SC.LOGIN_OK:
			handleLoginOk();
			break;
		case PROTOCOL.SC.ERROR:
			handleError(msgParts[1]);
			return;
		case PROTOCOL.SC.SEC_CONNECTION_ACCEPTED:
			handleSecConAccept(msgParts);
			return;
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

		for (UpdateListener updateListener : updateListeners) {
			updateListener.update();
		}
	}

	@Override
	public void connectionLost() {

	}

	private void handleSecConAccept(String[] msgParts) {
		mainAESKey = new SecretKeySpec(Encoder.b64Decode(msgParts[1]), "AES");
		isSecConAccepted = true;
		connectionListener.connected(this);
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
		for (ErrorListener errorListener : errorListeners) {
			errorListener.error(errorCode);
		}
	}

	private void handleConversation(String[] msgParts) {
		String conversationName = msgParts[1];
		String conversationId = msgParts[2];
		String convesationKey = msgParts[3];
		String[] usernames = Arrays.copyOfRange(msgParts, 3, msgParts.length);
		SecretKey conKey = new SecretKeySpec(convesationKey.getBytes(), "AES");
		cryptoSessions.add(new ClientCryptoConversation(conKey, conversationId));
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
		KeyPair kp = getRsaHandler().generateKeyPair();
		rsaPrivKey = kp.getPrivate();
		rsaPubKey = kp.getPublic();
		isRSA = true;
		send(PROTOCOL.buildMessage(PROTOCOL.CS.CREATE_SEC_CONNECTION, Encoder.b64Encode(rsaPubKey.getEncoded())));
	}

	public void addUpdateListener(UpdateListener updateListener) {
		updateListeners.add(updateListener);
	}

	public void addErrorListener(ErrorListener errorListener) {
		errorListeners.add(errorListener);
	}

	public byte[] XOR_ARR(byte[] a, byte[] b) {
		byte[] c = new byte[a.length];
		for (int i = 0; i < a.length - 1; i++) {
			c[i] = (byte) ((a[i] ^ b[i]) & 0x000000ff);
		}
		return c;
	}

	public byte XOR(byte a, byte b) {
		byte c = 0x0;
		c = (byte) ((a ^ b) & 0x000000ff);
		return c;
	}

	public byte[] generateSalt(String username, String password) {
		// Define Substitution box
		int[][] sbox = {
				{ 0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76 },
				{ 0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0 },
				{ 0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15 },
				{ 0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75 },
				{ 0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84 },
				{ 0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf },
				{ 0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8 },
				{ 0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2 },
				{ 0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73 },
				{ 0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb },
				{ 0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79 },
				{ 0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08 },
				{ 0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a },
				{ 0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e },
				{ 0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf },
				{ 0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16 } };
		// Define Galois Body
		int[][] galois = { { 0x02, 0x03, 0x01, 0x01 }, { 0x01, 0x02, 0x03, 0x01 }, { 0x01, 0x01, 0x02, 0x03 },
				{ 0x03, 0x01, 0x01, 0x02 } };
		// Remove Spaces from username
		username.replace(" ", "");
		// SHA512-Hash username and Password
		byte[] uHash = getHashHandler().getCheckSum(username.getBytes());
		byte[] pHash = getHashHandler().getCheckSum(password.getBytes());
		// Use PBKDF2 for combo hash
		byte[] comboHash = getHashHandler().getSecureHash(new String(uHash), pHash);
		// Create 2D Array
		byte[][] xor2D = new byte[256][256];
		// Fill the Array
		for (int i = 0; i < 255; i++) {
			// Each column linked to that before it
			byte[] thisColumn;
			if (i > 0) {
				thisColumn = getHashHandler().getCheckSum((new String(uHash) + new String(pHash)).getBytes());
			} else
				thisColumn = getHashHandler().getCheckSum(uHash);
			xor2D[i] = thisColumn;
			for (int x = 0; x < 256; x++) {
				// Each row element linked to the same element of the upper row
				if (i > 0) {
					xor2D[i][x] = XOR(xor2D[i][x], xor2D[i - 1][x]);
				}
			}
		}
		// Run Substitution/Permutation Network
		byte[] cKey = comboHash;
		for (int i = 0; i < 255; i++) {
			// Substitution
			for (int x = 0; i < xor2D.length; x++) {
				for (int j = 0; j < xor2D[0].length; j++) {
					int hex = xor2D[j][x];
					xor2D[j][x] = (byte) sbox[hex / 16][hex % 16];
				}
			}
			// Shift Rows
			for (int x = 1; x < xor2D.length; x++) {
				xor2D[x] = leftrotate(xor2D[x], x);
			}
			// Mix Columns
			mixColumns(xor2D, galois);
			// Link to cKey via XOR
			for (int x = 0; x < 255; x++) {
				xor2D[x] = XOR_ARR(xor2D[x], cKey);
			}
			// Generate next cKey
			cKey = XOR_ARR(xor2D[i], cKey);
		}
		// Diagonally Collapse the Table into an array
		byte[] snp = new byte[256];
		for (int i = 0; i < 255; i++) {
			snp[i] = xor2D[i][i];
		}
		// PBKDF2 Hash the combo hash with the snp array and return
		return getHashHandler().getSecureHash(new String(comboHash), snp);
	}

	public void mixColumns(byte[][] arr, int[][] galois) {
		byte[][] tarr = new byte[4][4];
		for (int i = 0; i < 4; i++) {
			System.arraycopy(arr[i], 0, tarr[i], 0, 4);
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				arr[i][j] = (byte) mcHelper(tarr, galois, i, j);
			}
		}
	}

	private int mcHelper(byte[][] arr, int[][] g, int i, int j) {
		int mcsum = 0;
		for (int k = 0; k < 4; k++) {
			int a = g[i][k];
			int b = arr[k][j];
			mcsum ^= mcCalc(a, b);
		}
		return mcsum;
	}

	private int mcCalc(int a, int b) {
		int[][] mc2 = {
				{ 0x00, 0x02, 0x04, 0x06, 0x08, 0x0a, 0x0c, 0x0e, 0x10, 0x12, 0x14, 0x16, 0x18, 0x1a, 0x1c, 0x1e },
				{ 0x20, 0x22, 0x24, 0x26, 0x28, 0x2a, 0x2c, 0x2e, 0x30, 0x32, 0x34, 0x36, 0x38, 0x3a, 0x3c, 0x3e },
				{ 0x40, 0x42, 0x44, 0x46, 0x48, 0x4a, 0x4c, 0x4e, 0x50, 0x52, 0x54, 0x56, 0x58, 0x5a, 0x5c, 0x5e },
				{ 0x60, 0x62, 0x64, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x72, 0x74, 0x76, 0x78, 0x7a, 0x7c, 0x7e },
				{ 0x80, 0x82, 0x84, 0x86, 0x88, 0x8a, 0x8c, 0x8e, 0x90, 0x92, 0x94, 0x96, 0x98, 0x9a, 0x9c, 0x9e },
				{ 0xa0, 0xa2, 0xa4, 0xa6, 0xa8, 0xaa, 0xac, 0xae, 0xb0, 0xb2, 0xb4, 0xb6, 0xb8, 0xba, 0xbc, 0xbe },
				{ 0xc0, 0xc2, 0xc4, 0xc6, 0xc8, 0xca, 0xcc, 0xce, 0xd0, 0xd2, 0xd4, 0xd6, 0xd8, 0xda, 0xdc, 0xde },
				{ 0xe0, 0xe2, 0xe4, 0xe6, 0xe8, 0xea, 0xec, 0xee, 0xf0, 0xf2, 0xf4, 0xf6, 0xf8, 0xfa, 0xfc, 0xfe },
				{ 0x1b, 0x19, 0x1f, 0x1d, 0x13, 0x11, 0x17, 0x15, 0x0b, 0x09, 0x0f, 0x0d, 0x03, 0x01, 0x07, 0x05 },
				{ 0x3b, 0x39, 0x3f, 0x3d, 0x33, 0x31, 0x37, 0x35, 0x2b, 0x29, 0x2f, 0x2d, 0x23, 0x21, 0x27, 0x25 },
				{ 0x5b, 0x59, 0x5f, 0x5d, 0x53, 0x51, 0x57, 0x55, 0x4b, 0x49, 0x4f, 0x4d, 0x43, 0x41, 0x47, 0x45 },
				{ 0x7b, 0x79, 0x7f, 0x7d, 0x73, 0x71, 0x77, 0x75, 0x6b, 0x69, 0x6f, 0x6d, 0x63, 0x61, 0x67, 0x65 },
				{ 0x9b, 0x99, 0x9f, 0x9d, 0x93, 0x91, 0x97, 0x95, 0x8b, 0x89, 0x8f, 0x8d, 0x83, 0x81, 0x87, 0x85 },
				{ 0xbb, 0xb9, 0xbf, 0xbd, 0xb3, 0xb1, 0xb7, 0xb5, 0xab, 0xa9, 0xaf, 0xad, 0xa3, 0xa1, 0xa7, 0xa5 },
				{ 0xdb, 0xd9, 0xdf, 0xdd, 0xd3, 0xd1, 0xd7, 0xd5, 0xcb, 0xc9, 0xcf, 0xcd, 0xc3, 0xc1, 0xc7, 0xc5 },
				{ 0xfb, 0xf9, 0xff, 0xfd, 0xf3, 0xf1, 0xf7, 0xf5, 0xeb, 0xe9, 0xef, 0xed, 0xe3, 0xe1, 0xe7, 0xe5 } };

		int[][] mc3 = {
				{ 0x00, 0x03, 0x06, 0x05, 0x0c, 0x0f, 0x0a, 0x09, 0x18, 0x1b, 0x1e, 0x1d, 0x14, 0x17, 0x12, 0x11 },
				{ 0x30, 0x33, 0x36, 0x35, 0x3c, 0x3f, 0x3a, 0x39, 0x28, 0x2b, 0x2e, 0x2d, 0x24, 0x27, 0x22, 0x21 },
				{ 0x60, 0x63, 0x66, 0x65, 0x6c, 0x6f, 0x6a, 0x69, 0x78, 0x7b, 0x7e, 0x7d, 0x74, 0x77, 0x72, 0x71 },
				{ 0x50, 0x53, 0x56, 0x55, 0x5c, 0x5f, 0x5a, 0x59, 0x48, 0x4b, 0x4e, 0x4d, 0x44, 0x47, 0x42, 0x41 },
				{ 0xc0, 0xc3, 0xc6, 0xc5, 0xcc, 0xcf, 0xca, 0xc9, 0xd8, 0xdb, 0xde, 0xdd, 0xd4, 0xd7, 0xd2, 0xd1 },
				{ 0xf0, 0xf3, 0xf6, 0xf5, 0xfc, 0xff, 0xfa, 0xf9, 0xe8, 0xeb, 0xee, 0xed, 0xe4, 0xe7, 0xe2, 0xe1 },
				{ 0xa0, 0xa3, 0xa6, 0xa5, 0xac, 0xaf, 0xaa, 0xa9, 0xb8, 0xbb, 0xbe, 0xbd, 0xb4, 0xb7, 0xb2, 0xb1 },
				{ 0x90, 0x93, 0x96, 0x95, 0x9c, 0x9f, 0x9a, 0x99, 0x88, 0x8b, 0x8e, 0x8d, 0x84, 0x87, 0x82, 0x81 },
				{ 0x9b, 0x98, 0x9d, 0x9e, 0x97, 0x94, 0x91, 0x92, 0x83, 0x80, 0x85, 0x86, 0x8f, 0x8c, 0x89, 0x8a },
				{ 0xab, 0xa8, 0xad, 0xae, 0xa7, 0xa4, 0xa1, 0xa2, 0xb3, 0xb0, 0xb5, 0xb6, 0xbf, 0xbc, 0xb9, 0xba },
				{ 0xfb, 0xf8, 0xfd, 0xfe, 0xf7, 0xf4, 0xf1, 0xf2, 0xe3, 0xe0, 0xe5, 0xe6, 0xef, 0xec, 0xe9, 0xea },
				{ 0xcb, 0xc8, 0xcd, 0xce, 0xc7, 0xc4, 0xc1, 0xc2, 0xd3, 0xd0, 0xd5, 0xd6, 0xdf, 0xdc, 0xd9, 0xda },
				{ 0x5b, 0x58, 0x5d, 0x5e, 0x57, 0x54, 0x51, 0x52, 0x43, 0x40, 0x45, 0x46, 0x4f, 0x4c, 0x49, 0x4a },
				{ 0x6b, 0x68, 0x6d, 0x6e, 0x67, 0x64, 0x61, 0x62, 0x73, 0x70, 0x75, 0x76, 0x7f, 0x7c, 0x79, 0x7a },
				{ 0x3b, 0x38, 0x3d, 0x3e, 0x37, 0x34, 0x31, 0x32, 0x23, 0x20, 0x25, 0x26, 0x2f, 0x2c, 0x29, 0x2a },
				{ 0x0b, 0x08, 0x0d, 0x0e, 0x07, 0x04, 0x01, 0x02, 0x13, 0x10, 0x15, 0x16, 0x1f, 0x1c, 0x19, 0x1a } };
		if (a == 1) {
			return b;
		} else if (a == 2) {
			return mc2[b / 16][b % 16];
		} else if (a == 3) {
			return mc3[b / 16][b % 16];
		}
		return 0;
	}

	private byte[] leftrotate(byte[] arr, int times) {
		assert (arr.length == 4);
		if (times % 4 == 0) {
			return arr;
		}
		while (times > 0) {
			int temp = arr[0];
			for (int i = 0; i < arr.length - 1; i++) {
				arr[i] = arr[i + 1];
			}
			arr[arr.length - 1] = (byte) temp;
			--times;
		}
		return arr;
	}

	public void login(String username, String password) {
		String pwdHash = Encoder.b64Encode(getHashHandler().getSecureHash(password, generateSalt(username, password)));
		thisUser = new User("", 0, username, pwdHash);
		sendAES(PROTOCOL.buildMessage(PROTOCOL.CS.LOGIN, thisUser.getUsername(), thisUser.getPasswordHash()));
	}

	public void register(String username, String passwordHash) {
		thisUser = new User("", 0, username, passwordHash);
		sendAES(PROTOCOL.buildMessage(PROTOCOL.CS.REGISTER, thisUser.getUsername(), thisUser.getPasswordHash()));
	}

	private void sendMessage(String convID, String content) {
		Conversation cCon = null;
		for (Conversation con : conversations) {
			if (con.getId().equals(convID)) {
				cCon = con;
			}
		}
		ClientCryptoConversation ccc = null;
		for (ClientCryptoConversation pccc : cryptoSessions) {
			if (pccc.getConversationID().equals(convID)) {
				ccc = pccc;
			}
		}
		content = false + PROTOCOL.SPLIT + content;
		byte[] cryp = getAESHandler().encrypt(content.getBytes(), ccc.getAesKey());
		String enc = Encoder.b64Encode(cryp);
		send(convID + PROTOCOL.SPLIT + enc);
	}

	public User getThisUser() {
		return thisUser;
	}

	public boolean isSecConAccepted() {
		return isSecConAccepted;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public ArrayList<Conversation> getConversations() {
		return conversations;
	}

	/**
	 * Sends the given message to the given user. The message is encrypted using the
	 * given key and the AES Algorithm
	 */
	private void sendAES(Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = getAESHandler().encrypt(msg.getBytes(), mainAESKey);
		String enc = Encoder.b64Encode(cryp);
		send(enc);
	}

	/**
	 * Sends the given message to the given user. The message is encrypted using the
	 * given key and the RSA Algorithm
	 */
	private void sendRSA(PublicKey key, Object... message) {
		String msg = PROTOCOL.buildMessage(message);
		byte[] cryp = getRsaHandler().encrypt(msg.getBytes(), key);
		String enc = Encoder.b64Encode(cryp);
		send(enc);
	}

	@FunctionalInterface
	public interface UpdateListener extends EventListener {
		void update();
	}

	@FunctionalInterface
	public interface ErrorListener extends EventListener {
		void error(int errorCode);
	}

	@FunctionalInterface
	public interface ConnectionListener extends EventListener {
		void connected(AppClient appClient);
	}
}
