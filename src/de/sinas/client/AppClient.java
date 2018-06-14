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
	private boolean loggedIn;

	public AppClient(String pServerIP, int pServerPort, Gui gui) {
		super(pServerIP, pServerPort);
		this.gui = gui;
	}

	@Override
	public void processMessage(String message) {
		String[] msgParts = message.split(PROTOCOL.SPLIT);
		switch (msgParts[0]) {
		case PROTOCOL.SC.OK:
			handleOk();
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

	private void handleOk() {
		if (authFile != null) {
			try {
				Files.delete(authFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			loggedIn = true;
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
			loggedIn = false;
			break;

		default:
			break;
		}
	}

	public void login() {
		try {
			File f = new File("T:\\Schulweiter Tausch\\" + InetAddress.getLocalHost().getHostAddress());
			f.createNewFile();
			authFile = f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		send(PROTOCOL.CS.LOGIN);
	}

	public User getThisUser() {
		return thisUser;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

}
