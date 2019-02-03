package de.sinas.net;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Hashtable;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.sinas.crypto.AESHandler;
import de.sinas.crypto.Encoder;
import de.sinas.crypto.RSAHandler;

public abstract class CryptoClient extends Client {
    private final AESHandler aesHandler = new AESHandler();
    private final RSAHandler rsaHandler = new RSAHandler();
    private boolean isRSA;
    private SecretKey mainAESKey;
    private PrivateKey rsaPrivateKey;
    private final Hashtable<String, SecretKey> conversationKeyTable = new Hashtable<>();
    private boolean isSecConAccepted;

    public CryptoClient(String serverIP, int serverPort) {
        super(serverIP, serverPort);
        makeSecureConnection();
    }

    @Override
    protected void processMessage(String message) {
        System.out.println("(CLIENT) New message: " + message);
        String[] msgParts = message.split(PROTOCOL.SPLIT, -1);
        if (isRSA) {
            String plainText = new String(rsaHandler.decrypt(Encoder.b64Decode(msgParts[0]), rsaPrivateKey));
            System.out.println("(CLIENT) Decrypted message: " + plainText);
            processRSAMessage(plainText);
        } else {
            if (msgParts.length == 1) {
                String plainText = new String(aesHandler.decrypt(Encoder.b64Decode(msgParts[0]), mainAESKey));
                System.out.println("(CLIENT) Decrypted message: " + plainText);
                processDecryptedMessage(plainText);
            } else {
                SecretKey cKey = conversationKeyTable.get(msgParts[0]);
                if (cKey != null) {
                    String plainText = new String(aesHandler.decrypt(Encoder.b64Decode(msgParts[1]), cKey));
                    System.out.println("(CLIENT) Decrypted message: " + plainText);
                    processDecryptedMessage(plainText);
                }
            }
        }
    }

    private void processRSAMessage(String message) {
        String[] msgParts = message.split(PROTOCOL.SPLIT, -1);
        if (!msgParts[0].equals(PROTOCOL.SC.SEC_CONNECTION_ACCEPTED)) {
            new IllegalStateException("Secure connection not accepted!").printStackTrace();
            System.exit(0);
        }
        mainAESKey = new SecretKeySpec(Encoder.b64Decode(msgParts[1]), "AES");
        isSecConAccepted = true;
        isRSA = false;
        processSecureConnected();
    }

    @Override
    protected void send(String message) {
        send(new Object[] { message });
    }

    protected void send(Object... message) {
        String msg = PROTOCOL.buildMessage(message);
        byte[] cryp = aesHandler.encrypt(msg.getBytes(), mainAESKey);
        String enc = Encoder.b64Encode(cryp);
        super.send(enc);
    }

    public void sendMessage(String convID, String message) {
        String content = PROTOCOL.buildMessage(PROTOCOL.CS.MESSAGE, convID, false, message);
        byte[] cryp = aesHandler.encrypt(content.getBytes(), conversationKeyTable.get(convID));
        String enc = Encoder.b64Encode(cryp);
        super.send(convID + PROTOCOL.SPLIT + enc);
    }

    private void makeSecureConnection() {
        if (isRSA || isSecConAccepted) {
            return;
        }
        KeyPair keyPair = rsaHandler.generateKeyPair();
        rsaPrivateKey = keyPair.getPrivate();
        PublicKey rsaPubKey = keyPair.getPublic();
        isRSA = true;
        super.send(PROTOCOL.buildMessage(PROTOCOL.CS.CREATE_SEC_CONNECTION, Encoder.b64Encode(rsaPubKey.getEncoded())));
    }

    protected void addConversationKey(String conversationID, String conversationKey) {
        SecretKey conKey = new SecretKeySpec(Encoder.b64Decode(conversationKey), "AES");
        conversationKeyTable.put(conversationID, conKey);
    }

    public boolean isSecConAccepted() {
        return isSecConAccepted;
    }

    protected abstract void processSecureConnected();

    protected abstract void processDecryptedMessage(String message);
}