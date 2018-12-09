package de.sinas.client.gui;

import de.sinas.client.AppClient;
import de.sinas.net.PROTOCOL;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JLabel statusLabel;
	private JButton registerButton;

	public LoginDialog() {
		setMinimumSize(new Dimension(600, 150));
		setBounds(0, 0, 700, 200);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblUsername = new JLabel("Username");
			GridBagConstraints gbc_lblUsername = new GridBagConstraints();
			gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
			gbc_lblUsername.anchor = GridBagConstraints.EAST;
			gbc_lblUsername.gridx = 0;
			gbc_lblUsername.gridy = 1;
			contentPanel.add(lblUsername, gbc_lblUsername);
		}
		{
			textField = new JTextField();
			GridBagConstraints gbc_textField = new GridBagConstraints();
			gbc_textField.insets = new Insets(0, 0, 5, 0);
			gbc_textField.fill = GridBagConstraints.HORIZONTAL;
			gbc_textField.gridx = 1;
			gbc_textField.gridy = 1;
			contentPanel.add(textField, gbc_textField);
			textField.setColumns(10);
		}
		{
			JLabel lblPassword = new JLabel("Password");
			GridBagConstraints gbc_lblPassword = new GridBagConstraints();
			gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
			gbc_lblPassword.anchor = GridBagConstraints.EAST;
			gbc_lblPassword.gridx = 0;
			gbc_lblPassword.gridy = 2;
			contentPanel.add(lblPassword, gbc_lblPassword);
		}
		{
			passwordField = new JPasswordField();
			GridBagConstraints gbc_passwordField = new GridBagConstraints();
			gbc_passwordField.insets = new Insets(0, 0, 5, 0);
			gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
			gbc_passwordField.gridx = 1;
			gbc_passwordField.gridy = 2;
			contentPanel.add(passwordField, gbc_passwordField);
		}
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.SOUTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel buttonPane = new JPanel();
				panel.add(buttonPane, BorderLayout.EAST);
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				{
					loginButton = new JButton("Login");
					loginButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onLoginButton();
						}
					});
					registerButton = new JButton("Register");
					registerButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onRegisterButton();
						}
					});
					buttonPane.add(registerButton);
					loginButton.setActionCommand("Login");
					buttonPane.add(loginButton);
					getRootPane().setDefaultButton(loginButton);
				}
				{
					JButton cancelButton = new JButton("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							onCancelButton();
						}
					});
					cancelButton.setActionCommand("Cancel");
					buttonPane.add(cancelButton);
				}
			}
			{
				statusLabel = new JLabel("");
				statusLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
				panel.add(statusLabel, BorderLayout.WEST);
			}
		}
	}

	private void onLoginButton() {
		if (textField.getText().isEmpty() || passwordField.getPassword().length == 0) {
			statusLabel.setText("<html><font color='red'>Enter your username and password!</font></html>");
			return;
		}
		setGuiEnabled(false);
		statusLabel.setText("Logging in...");
		AppClient appClient;
		appClient = new AppClient(PROTOCOL.IP, PROTOCOL.PORT, innerAppClient -> {
			innerAppClient.addErrorListener(errorCode -> {
				switch (errorCode) {
					case PROTOCOL.ERRORCODES.LOGIN_FAILED:
						statusLabel.setText("<html><font color='red'>Invalid username or password!</font></html>");
						break;
					default:
						statusLabel.setText("<html><font color='red'>Some error occurred! Error code: " + errorCode + "</font></html>");
						break;

				}
				setGuiEnabled(true);
				innerAppClient.close();
			});
			innerAppClient.addUpdateListener(() -> {
				if (innerAppClient.isLoggedIn()) {
					// TODO: open GUI
				}
			});
			innerAppClient.login(textField.getText(), new String(passwordField.getPassword()));

		});
		if (!appClient.isConnected()) {
			statusLabel.setText("<html><font color='red'>Can't reach the Server!</font></html>");
			setGuiEnabled(true);
			appClient.close();
		}
	}

	private void onRegisterButton() {
		if (textField.getText().isEmpty() || passwordField.getPassword().length == 0) {
			statusLabel.setText("<html><font color='red'>Enter your username and password!</font></html>");
			return;
		}
		setGuiEnabled(false);
		statusLabel.setText("Registering...");
		AppClient appClient;
		appClient = new AppClient(PROTOCOL.IP, PROTOCOL.PORT, innerAppClient -> {
			innerAppClient.addErrorListener(errorCode -> {
				switch (errorCode) {
					case PROTOCOL.ERRORCODES.ALREADY_REGISTERED:
						statusLabel.setText("<html><font color='red'>This user is already registered!</font></html>");
						break;
					default:
						statusLabel.setText("<html><font color='red'>Some error occurred! Error code: " + errorCode + "</font></html>");
						break;

				}
				setGuiEnabled(true);
				innerAppClient.close();
			});
			innerAppClient.addUpdateListener(() -> {
				if (innerAppClient.isLoggedIn()) {
					// TODO: open GUI
				}
			});
			innerAppClient.register(textField.getText(), new String(passwordField.getPassword()));

		});
		if (!appClient.isConnected()) {
			statusLabel.setText("<html><font color='red'>Can't reach the Server!</font></html>");
			setGuiEnabled(true);
			appClient.close();
		}
	}

	private void onCancelButton() {
		dispose();
	}

	private void setGuiEnabled(boolean enabled) {
		registerButton.setEnabled(enabled);
		loginButton.setEnabled(enabled);
		textField.setEnabled(enabled);
		passwordField.setEnabled(enabled);
	}
}
