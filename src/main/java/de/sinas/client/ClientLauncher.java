package de.sinas.client;

import javax.swing.JDialog;
import javax.swing.UIManager;

import de.sinas.client.gui.LoginDialog;
import de.sinas.client.gui.language.Language;

import java.util.Locale;

public class ClientLauncher {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			LoginDialog dialog = new LoginDialog(Language.getLanguage(Locale.getDefault()));
			dialog.setTitle("SiNaS - Login");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
