package de.sinas.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.client.AppClient;
import de.sinas.client.gui.language.Language;
import de.sinas.net.PROTOCOL;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class GUI extends JFrame {
	private JPanel contentPane;
	private JTextField messageTextField;
	private JList<Conversation> conversationsList;
	private Language lang;
	private AppClient appClient;
	private Conversation currentConversation;
	private JList<Message> messagesList;
	private JScrollPane messagesScrollPane;
	private JLabel conversationInfoLabel;

	public GUI(AppClient appClient, Language lang) {
		this.lang = lang;
		this.appClient = appClient;
		setTitle("SiNaS");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JScrollPane scrollPane = new JScrollPane() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 100);
			}

			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}
		};
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);

		conversationsList = new JList<Conversation>();
		conversationsList.addListSelectionListener(e -> onConversationSelected());
		conversationsList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Conversation conversation = (Conversation) value;
				return super.getListCellRendererComponent(list, "<html>" + conversation.getName() + "</html>", index, isSelected, cellHasFocus);
			}
		});
		conversationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(conversationsList);

		conversationInfoLabel = new JLabel("");
		conversationInfoLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_conversationLabel = new GridBagConstraints();
		gbc_conversationLabel.fill = GridBagConstraints.BOTH;
		gbc_conversationLabel.insets = new Insets(0, 0, 5, 5);
		gbc_conversationLabel.gridx = 1;
		gbc_conversationLabel.gridy = 1;
		contentPane.add(conversationInfoLabel, gbc_conversationLabel);
		
		JButton editButton = new JButton(lang.getString("edit"));
		editButton.addActionListener(e -> onEditConversation());
		GridBagConstraints gbc_editButton = new GridBagConstraints();
		gbc_editButton.fill = GridBagConstraints.BOTH;
		gbc_editButton.insets = new Insets(0, 0, 5, 0);
		gbc_editButton.gridx = 2;
		gbc_editButton.gridy = 1;
		contentPane.add(editButton, gbc_editButton);

		messagesScrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 2;
		contentPane.add(messagesScrollPane, gbc_scrollPane_1);

		messagesList = new JList<>();
		messagesList.setSelectionModel(new DefaultListSelectionModel() {
			@Override
			public void setSelectionInterval(int index0, int index1) {
				super.setSelectionInterval(-1, -1);
			}
		});
		messagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		messagesList.setSelectionBackground(new Color(230, 230, 230));
		messagesList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Message message = (Message) value;
				boolean isOwnMessage = message.getSender().equals(appClient.getThisUser().getUsername());
				setHorizontalAlignment(isOwnMessage ? RIGHT : LEFT);
				String string = String.format(
						"<html><div style=\"margin: 5; padding: 5; background: #aaaaaa; color: black; text-align: %s;\">%s<br>%s<br>%s</div></html>",
						isOwnMessage ? "right" : "left", message.getSender(), message.getContent(),
						DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.getTimestamp())));
				return super.getListCellRendererComponent(list, string, index, index % 2 == 0, cellHasFocus);
			}
		});
		messagesScrollPane.setViewportView(messagesList);

		JMenuBar menuBar = new JMenuBar();
		GridBagConstraints gbc_menuBar = new GridBagConstraints();
		gbc_menuBar.anchor = GridBagConstraints.WEST;
		gbc_menuBar.gridwidth = 3;
		gbc_menuBar.insets = new Insets(0, 0, 5, 0);
		gbc_menuBar.gridx = 0;
		gbc_menuBar.gridy = 0;
		contentPane.add(menuBar, gbc_menuBar);
		
		JMenu mnOptions = new JMenu(lang.getString("options"));
		menuBar.add(mnOptions);
		
		JMenuItem mntmAddConversation = new JMenuItem(lang.getString("add_conversation"));
		mntmAddConversation.addActionListener(e -> onAddConversation());
		mnOptions.add(mntmAddConversation);

		JMenu mnHelp = new JMenu(lang.getString("help"));
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem(lang.getString("about"));
		mnHelp.add(mntmAbout);

		messageTextField = new JTextField();
		messageTextField.addActionListener(e -> onMessageTextFieldAction());
		GridBagConstraints gbc_messageTextField = new GridBagConstraints();
		gbc_messageTextField.insets = new Insets(0, 0, 0, 5);
		gbc_messageTextField.fill = GridBagConstraints.BOTH;
		gbc_messageTextField.gridx = 1;
		gbc_messageTextField.gridy = 3;
		contentPane.add(messageTextField, gbc_messageTextField);
		messageTextField.setColumns(10);

		JButton sendButton = new JButton(lang.getString("send"));
		sendButton.addActionListener(e -> onSendButton());
		GridBagConstraints gbc_sendButton = new GridBagConstraints();
		gbc_sendButton.fill = GridBagConstraints.BOTH;
		gbc_sendButton.gridx = 2;
		gbc_sendButton.gridy = 3;
		contentPane.add(sendButton, gbc_sendButton);

		createUpdateListener();
		createErrorListener();

		appClient.requestConversations();
	}

	private void onAddConversation() {
		String conversationName = JOptionPane.showInputDialog(this, lang.getString("enter_conversation_name"), lang.getString("add_conversation"), JOptionPane.QUESTION_MESSAGE);
		if (conversationName == null || conversationName.equals("")) {
			return;
		}
		appClient.addConversation(conversationName);
	}

	private void onConversationSelected() {
		if (conversationsList.getSelectedValue() == null) {
			return;
		}
		if (conversationsList.getSelectedValue().equals(currentConversation)) {
			return;
		}
		currentConversation = conversationsList.getSelectedValue();
		messagesList.setListData(currentConversation.getMessages().toArray(new Message[0]));
		appClient.requestMessages(currentConversation.getId(), 1000);
		conversationInfoLabel.setText(String.format("<html><div style=\"padding: 5;\"><span style=\"font-size: 20;\">%s</span><br>%s</div></html>", currentConversation.getName(), String.join(", ", currentConversation.getUsers())));
	}

	private void onEditConversation() {
		if (currentConversation == null) {
			return;
		}
		ConversationEditDialog dialog = new ConversationEditDialog(this, appClient, currentConversation, lang);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (!dialog.showDialog()) {
			return;
		}
		if (!currentConversation.getName().equals(dialog.getConversationName())) {
			appClient.renameConversation(currentConversation.getId(), dialog.getConversationName());
		}
		List<String> newUsers = Arrays.asList(dialog.getUsers());
		for (String user : currentConversation.getUsers()) {
			if (!newUsers.contains(user)) {
				appClient.removeUserFromConversation(currentConversation.getId(), user);
			}
		}
		for (String user : newUsers) {
			if (!currentConversation.getUsers().contains(user)) {
				appClient.addUserToConversation(currentConversation.getId(), user);
			}
		}
	}

	private void onMessageTextFieldAction() {
		sendMessage();
	}

	private void onSendButton() {
		sendMessage();
	}

	private void sendMessage() {
		if (currentConversation == null) {
			return;
		}
		appClient.sendMessage(currentConversation.getId(), messageTextField.getText());
		messageTextField.setText("");
	}

	private void createUpdateListener() {
		appClient.addUpdateListener(msgBase -> {
			switch (msgBase) {
			case PROTOCOL.SC.CONVERSATION:
				onConversationUpdate();
				break;
			case PROTOCOL.SC.MESSAGES:
				onMessagesUpdate();
				break;
			}
		});
	}

	private void onConversationUpdate() {
		Conversation lastCurrentConversation = currentConversation;
		conversationsList.setListData(appClient.getConversations().toArray(new Conversation[0]));
		for (int i = 0; i < conversationsList.getModel().getSize(); i++) {
			if (conversationsList.getModel().getElementAt(i).equals(lastCurrentConversation)) {
				conversationsList.setSelectedIndex(i);
				break;
			}
		}
	}

	private void onMessagesUpdate() {
		if (currentConversation != null) {
			boolean scrollToBottom = messagesList.getLastVisibleIndex() == messagesList.getModel().getSize() - 1;
			messagesList.setListData(currentConversation.getMessages().toArray(new Message[0]));
			if (scrollToBottom) {
				SwingUtilities.invokeLater(() -> {
					messagesScrollPane.getVerticalScrollBar().setValue(Integer.MAX_VALUE);
				});
			}
		}
	}

	private void createErrorListener() {
		appClient.addErrorListener(errorCode -> {
			JOptionPane.showMessageDialog(this, lang.getString("error_code") + ": " + errorCode, lang.getString("some_error_occurred"), JOptionPane.ERROR_MESSAGE);
		});
	}
}
