package de.sinas.server;

import java.util.ArrayList;
import de.sinas.User;
import de.sinas.Conversation;

import de.sinas.server.ConversationCryptoSession;

public class ConversationCryptoManager {

    private ArrayList<ConversationCryptoSession> sessions = new ArrayList<ConversationCryptoSession>();

    public void addSession(ConversationCryptoSession ccs) {
        sessions.add(ccs);
    }

    public void removeSession(ConversationCryptoSession ccs) {
        sessions.remove(ccs);
    }

    public boolean hasSession(User u, Conversation conv) {
        for (ConversationCryptoSession ccs : sessions) {
            if(ccs.getOwner() == u && ccs.getConv() == conv) {
                return true;
            }
        }
        return false;
    }

    public ConversationCryptoSession getSession(User u, Conversation conv) {
        for (ConversationCryptoSession ccs : sessions) {
            if(ccs.getOwner() == u && ccs.getConv() == conv) {
                return ccs;
            }
        }
        return null;
    }
}