package de.sinas.server;

import java.io.File;

import de.sinas.net.PROTOCOL;

public class ServerLauncher {

	public static void main(String[] args) {
		new AppServer(PROTOCOL.PORT, new File(args[0]), new File(args[1]));
	}

}
