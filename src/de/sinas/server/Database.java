package de.sinas.server;

import java.io.File;

import de.sinas.Conversation;
import de.sinas.User;

public class Database {
    private File databaseDirectory;

    public Database(File databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public User getUser(String username) {
        return null;
    }

    public Conversation[] getConversations(User user) {
        return null;
    }

    public void saveUser(User user) {

    }

    public void saveConversation(Conversation conversation) {

    }
}
