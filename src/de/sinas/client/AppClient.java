package de.sinas.client;

import de.sinas.net.Client;

public class AppClient extends Client {

	public AppClient(String pServerIP, int pServerPort) {
		super(pServerIP, pServerPort);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processMessage(String pMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionLost() {
		// TODO Auto-generated method stub
		
	}

}
