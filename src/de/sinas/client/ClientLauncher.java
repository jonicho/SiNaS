package de.sinas.client;

import de.sinas.client.gui.LoginDialog;

public class ClientLauncher {

	public static void main(String[] args) {
		LoginDialog loginDialog = new LoginDialog();
		loginDialog.pack();
		loginDialog.setLocationRelativeTo(null);
		loginDialog.setVisible(true);
	}
}
