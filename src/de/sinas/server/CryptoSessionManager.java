package de.sinas.server;

import java.util.ArrayList;
import de.sinas.User;

public class CryptoSessionManager {
    private ArrayList<CryptoSession> sessions = new ArrayList<CryptoSession>();

    public CryptoSession getSessionByUser(User u) {
        for(CryptoSession cs : sessions) {
            if(cs.getOwner().equals(u)) {
                return cs;
            }
        }
        return null;
    }

    public void addSession(CryptoSession cs) {
        if(cs != null) {
            sessions.add(cs);
        }
    }

    public void removeSession(CryptoSession cs) {
        sessions.remove(cs);
    }
}