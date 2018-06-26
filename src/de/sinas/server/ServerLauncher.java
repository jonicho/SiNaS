package de.sinas.server;

import java.io.File;

import de.sinas.net.PROTOCOL;

public class ServerLauncher {
	
	public static void main(String[] args) {
		new AppServer(PROTOCOL.PORT, new File("/home/jonas/SiNaS-Database"), new File("/home/jonas"));
	}

}
