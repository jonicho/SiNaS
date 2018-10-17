package de.sinas.server;

import javax.crypto.SecretKey;
import java.security.PublicKey;
import de.sinas.User;

public class CryptoSession {

    private SecretKey userPublicKey;
    private SecretKey mainAESKey;
    private User owner;

    public CryptoSession(User u) {
        owner = u;
    }   

    /**
     * @return the owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * @return the mainAESKey
     */
    public SecretKey getMainAESKey() {
        return mainAESKey;
    }

    /**
     * @return the userPublicKey
     */
    public SecretKey getUserPublicKey() {
        return userPublicKey;
    }

    /**
     * @param mainAESKey the mainAESKey to set
     */
    public void setMainAESKey(SecretKey mainAESKey) {
        this.mainAESKey = mainAESKey;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * @param userPublicKey the userPublicKey to set
     */
    public void setUserPublicKey(SecretKey userPublicKey) {
        this.userPublicKey = userPublicKey;
    }
     
   

}