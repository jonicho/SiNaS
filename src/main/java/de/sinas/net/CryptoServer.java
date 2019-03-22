package de.sinas.net;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.crypto.SecretKey;

import de.sinas.Conversation;
import de.sinas.User;
import de.sinas.Users;
import de.sinas.crypto.AESHandler;
import de.sinas.crypto.Encoder;
import de.sinas.crypto.RSAHandler;
import de.sinas.server.TempUser;

public abstract class CryptoServer extends Server {
    private final Hashtable<User, PublicKey> publicKeys = new Hashtable<>();
    private final Hashtable<User, SecretKey> aesKeys = new Hashtable<>();
    private final Hashtable<String, SecretKey> conversationUserKeys = new Hashtable<>();
    private final AESHandler aesHandler = new AESHandler();
    private final RSAHandler rsaHandler = new RSAHandler();
    protected final Users users = new Users();
    private final ArrayList<TempUser> tempUsers = new ArrayList<TempUser>();

    public CryptoServer(int port) {
        super(port);
    }

    @Override
    public void processMessage(String clientIP, int clientPort, String message) {
        System.out.println("(SERVER) New message: " + clientIP + ":" + clientPort + ", " + message);
        String[] msgParts = message.split(PROTOCOL.SPLIT, -1);
        User user = users.getUser(clientIP, clientPort);
        if (user != null && aesKeys.get(user) != null) {
            SecretKey key;
            String encodedMessage;
            if (msgParts.length == 1) {
                key = aesKeys.get(user);
                encodedMessage = msgParts[0];
            } else {
                key = getConversationUserKey(user.getIp(), user.getPort(), msgParts[0]);
                encodedMessage = msgParts[1];
            }
            String decodedMessage = new String(aesHandler.decrypt(Encoder.b64Decode(encodedMessage), key));
            System.out.println("(SERVER) Decoded message: " + clientIP + ":" + clientPort + ", " + decodedMessage);
            processDecryptedMessage(user, decodedMessage);
            return;
        }
        if (user != null) {
            return;
        }
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
                sendErrorUnencrypted(new TempUser(clientIP, clientPort), PROTOCOL.ERRORCODES.NOT_SEC_CONNECTED);
                return;
            }
            String decodedMessage = new String(
                    aesHandler.decrypt(Encoder.b64Decode(msgParts[0]), tempUser.getAesKey()));
            System.out.println("(SERVER) Decoded message: " + clientIP + ":" + clientPort + ", " + decodedMessage);
            processDecryptedMessage(tempUser, decodedMessage);
        }
    }

    /**
     * Handles the request to create a secure conversation. <br>
     * Uses the RSA public key that was received with the request to send <br>
     * an RSA encrypted AES256 key that can be used for further conversation.
     * @param tUser the Temporary User who sent the Request
     * @param msgParts the Request
     */
    private void handleCreateSecConnection(TempUser tUser, String[] msgParts) {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Encoder.b64Decode(msgParts[1]));
        try {
            KeyFactory keyFact = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFact.generatePublic(keySpec);
            tUser.setRsaKey(pubKey);
            tUser.setAesKey(aesHandler.generateKey());
            tempUsers.add(tUser);
        } catch (Exception ex) {
            ex.printStackTrace();
            sendError(tUser, PROTOCOL.ERRORCODES.UNKNOWN_ERROR);
            return;
        }
        sendRSA(new User(tUser.getIp(), tUser.getPort(), "", ""), tUser.getRsaKey(), PROTOCOL
                .buildMessage(PROTOCOL.SC.SEC_CONNECTION_ACCEPTED, Encoder.b64Encode(tUser.getAesKey().getEncoded())));
    }

    /**
     * Sends the given message to the given user. The message is encrypted using the
     * given key and the AES Algorithm
     */
    protected void send(User user, Object... message) {
        String msg = PROTOCOL.buildMessage(message);
        byte[] cryp = aesHandler.encrypt(msg.getBytes(),
                user instanceof TempUser ? ((TempUser) user).getAesKey() : aesKeys.get(user));
        String enc = Encoder.b64Encode(cryp);
        send(user.getIp(), user.getPort(), enc);
    }

    /**
     * Sends the given message to the given user. The message is encrypted using the
     * given key and the RSA Algorithm
     */
    private void sendRSA(User user, PublicKey key, Object... message) {
        String msg = PROTOCOL.buildMessage(message);
        byte[] cryp = rsaHandler.encrypt(msg.getBytes(), key);
        String enc = Encoder.b64Encode(cryp);
        send(user.getIp(), user.getPort(), enc);
    }

    /**
     * Sends a given message to all participants of a conversation.
     * @param con the target conversation
     * @param message the message to be sent
     */
    protected void sendToConversation(Conversation con, Object... message) {
        for (String username : con.getUsers()) {
            User user = users.getUser(username);
            if (user == null) {
                continue;
            }
            SecretKey key = getConversationUserKey(user.getIp(), user.getPort(), con.getId());
            if (key == null) {
                continue;
            }
            String msg = PROTOCOL.buildMessage(message);
            byte[] cryp = aesHandler.encrypt(msg.getBytes(), key);
            String enc = Encoder.b64Encode(cryp);
            send(user.getIp(), user.getPort(), con.getId() + PROTOCOL.SPLIT + enc);
        }
    }

    /**
     * Sends the given error code to the given user.
     */
    protected void sendError(User user, int errorCode) {
        send(user, new Object[] { PROTOCOL.getErrorMessage(errorCode) });
    }

    /**
     * Sends the given error code to the given user without encryption.
     */
    private void sendErrorUnencrypted(User user, int errorCode) {
        send(user.getIp(), user.getPort(), PROTOCOL.getErrorMessage(errorCode));
    }

    /**
     * Adds the Cryptographic Keys to a User object.
     * @param user The target user
     * @param publicKey the RSA public key
     * @param aesKey the AES256 main key
     */
    protected void addUserKeys(User user, PublicKey publicKey, SecretKey aesKey) {
        publicKeys.put(user, publicKey);
        aesKeys.put(user, aesKey);
        TempUser tempUser = null;
        for (TempUser tu : tempUsers) {
            if (tu.getIp().equals(user.getIp()) && tu.getPort() == user.getPort()) {
                tempUser = tu;
                break;
            }
        }
        tempUsers.remove(tempUser);
    }

    /**
     * Gets the AES256 Conversation key for a specified conversation and client.
     * @param clientIP the clients IP
     * @param clientPort the clients Ports
     * @param conversationId the Conversations ID
     * @return
     */
    public SecretKey getConversationUserKey(String clientIP, int clientPort, String conversationId) {
        SecretKey key = conversationUserKeys.get(clientIP + ":" + clientPort + "@" + conversationId);
        if (key == null) {
            key = aesHandler.generateKey();
            conversationUserKeys.put(clientIP + ":" + clientPort + "@" + conversationId, key);
        }
        return key;
    }

    protected abstract void processDecryptedMessage(User user, String message);
}
