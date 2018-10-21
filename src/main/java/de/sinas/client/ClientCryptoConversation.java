package de.sinas.client;

import javax.crypto.SecretKey;

public class ClientCryptoConversation {
    private SecretKey aesKey;
    private String conversationID;

    public ClientCryptoConversation(SecretKey pKey,String pID) {
        aesKey = pKey;
        conversationID = pID;
    }

    /**
     * @return the aesKey
     */
    public SecretKey getAesKey() {
        return aesKey;
    }

    /**
     * @return the conversationID
     */
    public String getConversationID() {
        return conversationID;
    }
}

