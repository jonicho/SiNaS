package de.sinas.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;

import de.sinas.User;
import de.sinas.net.PROTOCOL;
import de.sinas.net.Server;

public class AppServer extends Server {
	private Database db = new Database(new File("C:\\Users\\jan-frederic.ruhl\\Desktop\\SiNaS-Database"));
	private Users users = new Users();

	public AppServer(int pPort) {
		super(pPort);
	}

	@Override
	public void processNewConnection(String clientIP, int clientPort) {
		System.out.println("New connection: " + clientIP + ":" + clientPort);
		if(!users.doesUserExist(clientIP, clientPort)) {
			users.addUser(new User(clientIP,clientPort,"",""));
		}
	}

	@Override
	public void processMessage(String clientIP, int clientPort, String message) {
		System.out.println("New message: " + clientIP + ":" + clientPort + ", " + message);
		User user = users.getUser(clientIP, clientPort);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		switch (msgParts[0]) {
		case PROTOCOL.CS.MSG:
			handleMessage(msgParts);
			break;
		case PROTOCOL.CS.LOGIN:
			handleLogin(user);
			if(user.isAuthed()) {
				send(clientIP, clientPort, PROTOCOL.buildMessage(PROTOCOL.SC.LOGIN_OK,user.getUsername()));
			}
			else send(clientIP, clientPort, PROTOCOL.buildMessage(PROTOCOL.SC.ERROR,PROTOCOL.ERRORCODES.LOGIN_FAILED));
			break;
		default:
			break;
		}
	}
	
	
	private void handleLogin(User pUser) {
		if(pUser.isAuthed()) {
			return;
		}
		Path path = Paths.get("T:\\Schulweiter Tausch\\"+pUser.getIp());
        FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
        try {
        	UserPrincipal owner = ownerAttributeView.getOwner();
        	String[] splt = owner.getName().split("\\");
			pUser.setUsername(splt[1]);
			pUser.setAuthed(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	private void handleMessage(String[] msgParts) {
		
	}
	
	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
	}
}
