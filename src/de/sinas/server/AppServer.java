package de.sinas.server;

import java.io.File;

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

		default:
			break;
		}
	}
	
	private void handleLogin() {
		
	}
	
	private void handleMessage(String[] msgParts) {
		
	}
	
	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
	}
}
