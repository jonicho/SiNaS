package de.sinas.client;

import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.UIManager;

import de.sinas.Logger;
import de.sinas.client.gui.LoginDialog;
import de.sinas.client.gui.language.Language;

public class ClientLauncher {

	public static void main(String[] args) {
		Logger.init(false);
		Logger.log("Sarting client...");
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
