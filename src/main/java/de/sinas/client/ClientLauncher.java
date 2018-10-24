package de.sinas.client;

import de.sinas.net.PROTOCOL;

public class ClientLauncher {

	public static void main(String[] args) {
		AppClient appClient = new AppClient("localhost", PROTOCOL.PORT);
	}
}
