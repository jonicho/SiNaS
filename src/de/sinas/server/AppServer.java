package de.sinas.server;

import de.sinas.net.PROTOCOL;
import de.sinas.net.Server;

public class AppServer extends Server {

	public AppServer(int pPort) {
		super(pPort);
	}

	@Override
	public void processNewConnection(String pClientIP, int pClientPort) {
		System.out.println("New connection: " + pClientIP + ":" + pClientPort);
	}

	@Override
	public void processMessage(String pClientIP, int pClientPort, String pMessage) {
		System.out.println("New message: " + pClientIP + ":" + pClientPort + ", " + pMessage);
	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		System.out.println("Closing connection: " + pClientIP + ":" + pClientPort);
	}

	public static void main(String[] args) {
		new AppServer(PROTOCOL.PORT);
	}
}
