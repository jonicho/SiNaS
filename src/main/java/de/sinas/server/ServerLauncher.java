package de.sinas.server;

import de.sinas.Logger;
import de.sinas.net.PROTOCOL;

public class ServerLauncher {

	public static void main(String[] args) {
		Logger.init(true);
		Logger.log("Starting Server...");
		new AppServer(PROTOCOL.PORT, "SiNaS-Database.db");
	}

}
