package de.sinas.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;

import de.sinas.User;
import de.sinas.client.gui.Gui;
import de.sinas.net.Client;
import de.sinas.net.PROTOCOL;

public class AppClient extends Client {
	private Gui gui;
	private User thisUser;
	private File authFile;

	public AppClient(String pServerIP, int pServerPort, Gui gui) {
		super(pServerIP, pServerPort);
		this.gui = gui;
	}

	@Override
	public void processMessage(String message) {
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		switch (msgParts[0]) {
		case PROTOCOL.SC.LOGIN_OK:
			handleLoginOk(msgParts[1], msgParts[2]);
			break;
		case PROTOCOL.SC.ERROR:
			handleError(msgParts[1]);
			break;
		default:
			break;
		}

	}

	@Override
	public void connectionLost() {

	}

	private void handleLoginOk(String username, String nickname) {
		if (authFile != null) {
			try {
				Files.delete(authFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			thisUser = new User(null, 0, username, username);
		}
	}

	private void handleError(String error) {
		int errorCode;
		try {
			errorCode = Integer.parseInt(error);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		switch (errorCode) {
		case PROTOCOL.ERRORCODES.LOGIN_FAILED:
			try {
				Files.delete(authFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			thisUser = null;
			break;

		default:
			break;
		}
	}

	public void login() {
		try {
			File f = new File("T:\\Schulweiter Tausch\\" + InetAddress.getLocalHost().getHostAddress());
			if (f.exists()) {
				Files.delete(f.toPath());
			}
			f.createNewFile();
			authFile = f;
			send(PROTOCOL.buildMessage(PROTOCOL.CS.LOGIN));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public User getThisUser() {
		return thisUser;
	}

	public boolean isLoggedIn() {
		return thisUser != null;
	}

}
