package de.sinas.server;

import javax.crypto.spec.SecretKeySpec;
import java.security.PublicKey;
import de.sinas.User;

public class CryptoSession {

    private PublicKey userPublicKey;
    private SecretKeySpec mainAESKey;
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
    public SecretKeySpec getMainAESKey() {
        return mainAESKey;
    }

    /**
     * @return the userPublicKey
     */
    public PublicKey getUserPublicKey() {
        return userPublicKey;
    }

    /**
     * @param mainAESKey the mainAESKey to set
     */
    public void setMainAESKey(SecretKeySpec mainAESKey) {
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
    public void setUserPublicKey(PublicKey userPublicKey) {
        this.userPublicKey = userPublicKey;
    }
     
   

}