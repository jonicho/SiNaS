package de.sinas.server;

import de.sinas.net.Server;

public class AppServer extends Server{

	public AppServer(int pPort) {
		super(pPort);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processNewConnection(String pClientIP, int pClientPort) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processMessage(String pClientIP, int pClientPort, String pMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processClosingConnection(String pClientIP, int pClientPort) {
		// TODO Auto-generated method stub
		
	}

}
