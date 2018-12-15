package de.sinas.server;

import javax.crypto.SecretKey;

import de.sinas.Conversation;
import de.sinas.User;

public class ConversationCryptoSession {

    private Conversation conv;
    private User owner;
    private SecretKey aesKey;

    public ConversationCryptoSession(Conversation pConv, User pUser) {
        conv = pConv;
        owner = pUser;
    }

    /**
     * @return the aesKey
     */
    public SecretKey getAesKey() {
        return aesKey;
    }

    /**
     * @return the conv
     */
    public Conversation getConv() {
        return conv;
    }

    /**
     * @return the owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * @param aesKey the aesKey to set
     */
    public void setAesKey(SecretKey aesKey) {
        this.aesKey = aesKey;
    }

    /**
     * @param conv the conv to set
     */
    public void setConv(Conversation conv) {
        this.conv = conv;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

}