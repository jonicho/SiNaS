package de.sinas.client;

import de.sinas.User;
import de.sinas.client.gui.Gui;
import de.sinas.net.Client;
import de.sinas.net.PROTOCOL;

public class AppClient extends Client {
	private Gui gui;
	private User thisUser;

	public AppClient(String pServerIP, int pServerPort, Gui gui) {
		super(pServerIP, pServerPort);
		this.gui = gui;
	}

	@Override
	public void processMessage(String message) {
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		switch (msgParts[0]) {
		case "":
			
			break;

		default:
			break;
		}

	}

	@Override
	public void connectionLost() {

	}
	
	public User getThisUser() {
		return thisUser;
	}
	
}
