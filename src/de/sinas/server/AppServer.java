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
	private Database db = new Database(new File("C:\\Users\\jonas.keller\\Desktop\\SiNaS-Database"));
	private Users users = new Users();

	public AppServer(int pPort) {
		super(pPort);
	}

	@Override
	public void processNewConnection(String clientIP, int clientPort) {
		System.out.println("New connection: " + clientIP + ":" + clientPort);
	}

	@Override
	public void processMessage(String clientIP, int clientPort, String message) {
		System.out.println("New message: " + clientIP + ":" + clientPort + ", " + message);
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		User user = users.getUser(clientIP, clientPort);
		if (user == null) {
			if (!msgParts[0].equals(PROTOCOL.CS.LOGIN)) {
				send(clientIP, clientPort, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.NOT_LOGGED_IN));
			} else {
				handleLogin(clientIP, clientPort);
			}
			return;
		}
		switch (msgParts[0]) {
		case PROTOCOL.CS.MSG:
			handleMessage(user, msgParts);
			break;
		default:
			break;
		}
	}

	private void handleLogin(String clientIP, int clientPort) {
		Path path = Paths.get("T:\\Schulweiter Tausch\\" + clientIP);
		FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
		try {
			String[] ownerName = ownerAttributeView.getOwner().getName().split("\\\\");
			User user = db.getUser(ownerName[ownerName.length - 1], clientIP, clientPort);
			send(user.getIp(), user.getPort(),
					PROTOCOL.buildMessage(PROTOCOL.SC.LOGIN_OK, user.getUsername(), user.getNickname()));
		} catch (IOException e) {
			e.printStackTrace();
			send(clientIP, clientPort, PROTOCOL.getErrorMessage(PROTOCOL.ERRORCODES.LOGIN_FAILED));
		}

	}

	private void handleMessage(User user, String[] msgParts) {

	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
	}
}
