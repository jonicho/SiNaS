package de.sinas.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.client.AppClient;
import de.sinas.client.gui.language.Language;
import de.sinas.net.PROTOCOL;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {
	private JPanel contentPane;
	private JTextField messageTextField;
	private JList<Conversation> conversationsList;
	private Language lang;
	private AppClient appClient;
	private GUIConversation currentConversation;
	private JLabel conversationInfoLabel;
	private boolean messagesUpdating = false;
	private List<GUIConversation> guiConversations = new ArrayList<>();
	private JButton sendButton;

	public GUI(AppClient appClient, Language lang) {
		this.lang = lang;
		this.appClient = appClient;
		setTitle("SiNaS");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				appClient.close();
				System.exit(0);
			}
		});
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
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
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

		JMenuBar menuBar = new JMenuBar();
		GridBagConstraints gbc_menuBar = new GridBagConstraints();
		gbc_menuBar.anchor = GridBagConstraints.WEST;
		gbc_menuBar.gridwidth = 3;
		gbc_menuBar.insets = new Insets(0, 0, 5, 0);
		gbc_menuBar.gridx = 0;
		gbc_menuBar.gridy = 0;
		contentPane.add(menuBar, gbc_menuBar);

		JMenu mnHelp = new JMenu(lang.getString("help"));
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem(lang.getString("about"));
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAbout();
			}
		});
		mnHelp.add(mntmAbout);

		messageTextField = new JTextField();
		messageTextField.setEnabled(false);
		messageTextField.addActionListener(e -> onMessageTextFieldAction());
		
		JButton btnAddconversation = new JButton(lang.getString("add_conversation"));
		btnAddconversation.addActionListener(e -> onAddConversation());
		GridBagConstraints gbc_btnAddconversation = new GridBagConstraints();
		gbc_btnAddconversation.fill = GridBagConstraints.BOTH;
		gbc_btnAddconversation.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddconversation.gridx = 0;
		gbc_btnAddconversation.gridy = 3;
		contentPane.add(btnAddconversation, gbc_btnAddconversation);
		
		GridBagConstraints gbc_messageTextField = new GridBagConstraints();
		gbc_messageTextField.insets = new Insets(0, 0, 0, 5);
		gbc_messageTextField.fill = GridBagConstraints.BOTH;
		gbc_messageTextField.gridx = 1;
		gbc_messageTextField.gridy = 3;
		contentPane.add(messageTextField, gbc_messageTextField);
		messageTextField.setColumns(10);

		sendButton = new JButton(lang.getString("send"));
		sendButton.setEnabled(false);
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

	private void onAbout() {
		AboutDialog dialog = new AboutDialog(lang);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
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
		if (currentConversation != null && conversationsList.getSelectedValue().equals(currentConversation.conversation)) {
			return;
		}
		if (messagesUpdating) { // if the messages are being updated, try again later
			SwingUtilities.invokeLater(() -> {
				onConversationSelected();
			});
			return;
		}
		messagesUpdating = true;
		if (currentConversation != null) {
			currentConversation.hide();
		}
		currentConversation = getGUIConversation(conversationsList.getSelectedValue());
		currentConversation.show();
		sendButton.setEnabled(true);
		messageTextField.setEnabled(true);
		updateConversationInfoLabel();
		if (currentConversation.conversation.getMessages().isEmpty()) {
			appClient.requestMessages(currentConversation.conversation.getId(), System.currentTimeMillis(), 20);
		}
		messagesUpdating = false;
	}

	private void onMessagesScrolled(AdjustmentEvent e) {
		if (currentConversation == null || currentConversation.conversation.getMessages().isEmpty() || messagesUpdating) {
			return;
		}
		if (currentConversation.messagesScrollPane.getVerticalScrollBar().getValue() == 0 && !e.getValueIsAdjusting()) {
			appClient.requestMessages(currentConversation.conversation.getId(), currentConversation.conversation.getMessages().get(0).getTimestamp(), 20);
		}
	}

	private void onEditConversation() {
		if (currentConversation == null) {
			return;
		}
		ConversationEditDialog dialog = new ConversationEditDialog(this, appClient, currentConversation.conversation, lang);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		if (!dialog.showDialog()) {
			return;
		}
		if (!currentConversation.conversation.getName().equals(dialog.getConversationName())) {
			appClient.renameConversation(currentConversation.conversation.getId(), dialog.getConversationName());
		}
		List<String> newUsers = Arrays.asList(dialog.getUsers());
		for (String user : currentConversation.conversation.getUsers()) {
			if (!newUsers.contains(user)) {
				appClient.removeUserFromConversation(currentConversation.conversation.getId(), user);
			}
		}
		for (String newUser : newUsers) {
			if (!currentConversation.conversation.getUsers().contains(newUser)) {
				appClient.addUserToConversation(currentConversation.conversation.getId(), newUser);
			}
		}
	}

	private void onMessageTextFieldAction() {
		sendMessage();
	}

	private void onSendButton() {
		sendMessage();
	}

	private void updateConversationInfoLabel() {
		if (currentConversation == null) {
			conversationInfoLabel.setText("");
			return;
		}
		conversationInfoLabel.setText(String.format("<html><div style=\"padding: 5;\"><span style=\"font-size: 20;\">%s</span><br>%s</div></html>", currentConversation.conversation.getName(), String.join(", ", currentConversation.conversation.getUsers())));
	}

	private void scrollToMessage(Message messageToScrollTo, Runnable runWhenFinishedScrolling) {
		int index = 0;
		if (messageToScrollTo != null) {
			for (int i = 0; i < currentConversation.messagesList.getModel().getSize(); i++) {
				if (currentConversation.messagesList.getModel().getElementAt(i).equals(messageToScrollTo)) {
					index = i;
					break;
				}
			}
		} else {
			index = currentConversation.messagesList.getModel().getSize() - 1;
		}
		int indexToScrollTo = index;
		SwingUtilities.invokeLater(() -> {
			if (indexToScrollTo < currentConversation.messagesList.getFirstVisibleIndex()
					|| indexToScrollTo > currentConversation.messagesList.getLastVisibleIndex()) {
				currentConversation.messagesList.ensureIndexIsVisible(indexToScrollTo);
			}
			runWhenFinishedScrolling.run();
		});
	}

	private void sendMessage() {
		if (currentConversation == null || StringUtils.isBlank(messageTextField.getText())) {
			return;
		}
		appClient.sendMessage(currentConversation.conversation.getId(), messageTextField.getText());
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
		conversationsList.setListData(appClient.getConversations().toArray(new Conversation[0]));
		for (Conversation c : appClient.getConversations()) {
			if (getGUIConversation(c) == null) {
				guiConversations.add(new GUIConversation(c));
			}
		}
		guiConversations.removeAll(guiConversations.stream().filter(gC -> !appClient.getConversations().contains(gC.conversation)).collect(Collectors.toList()));
		Conversation lastCurrentConversation;
		if (currentConversation == null) {
			lastCurrentConversation = null;
		} else {
			lastCurrentConversation = currentConversation.conversation;
		}
		for (int i = 0; i < conversationsList.getModel().getSize(); i++) {
			if (conversationsList.getModel().getElementAt(i).equals(lastCurrentConversation)) {
				conversationsList.setSelectedIndex(i);
				currentConversation = getGUIConversation(conversationsList.getModel().getElementAt(i));
				updateConversationInfoLabel();
				break;
			}
		}
		if (currentConversation != null && !guiConversations.contains(currentConversation)) {
			currentConversation.hide();
			currentConversation = null;
			updateConversationInfoLabel();
		}
	}

	private void onMessagesUpdate() {
		if (currentConversation != null) {
			if (messagesUpdating) { // if the messages are being updated, try again later
				SwingUtilities.invokeLater(() -> {
					onMessagesUpdate();
				});
				return;
			}
			messagesUpdating = true;
			Message messageToScrollTo = null;
			if (currentConversation.messagesList.getModel().getSize() > 0 && currentConversation.messagesList.getLastVisibleIndex() != currentConversation.messagesList.getModel().getSize() - 1) {
				messageToScrollTo = currentConversation.messagesList.getModel().getElementAt(currentConversation.messagesList.getFirstVisibleIndex());
			}
			currentConversation.messagesList.setListData(currentConversation.conversation.getMessages().toArray(new Message[0]));
			scrollToMessage(messageToScrollTo, () -> messagesUpdating = false);
		}
	}
	
	private void createErrorListener() {
		appClient.addErrorListener(errorCode -> {
			JOptionPane.showMessageDialog(this, lang.getString("error_code") + ": " + errorCode, lang.getString("some_error_occurred"), JOptionPane.ERROR_MESSAGE);
		});
	}

	private GUIConversation getGUIConversation(Conversation conversation) {
		for (GUIConversation gConv : guiConversations) {
			if (gConv.conversation.equals(conversation)) {
				return gConv;
			}
		}
		return null;
	}

	private class GUIConversation {
		private final Conversation conversation;
		private final JList<Message> messagesList;
		private final JScrollPane messagesScrollPane;
		private final GridBagConstraints gbc_scrollPane_1;

		private GUIConversation(Conversation conversation) {
			this.conversation = conversation;
			messagesScrollPane = new JScrollPane();
			messagesScrollPane.getVerticalScrollBar().addAdjustmentListener(e -> onMessagesScrolled(e));
			messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
			gbc_scrollPane_1 = new GridBagConstraints();
			gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
			gbc_scrollPane_1.gridwidth = 2;
			gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane_1.gridx = 1;
			gbc_scrollPane_1.gridy = 2;
	
			messagesList = new JList<Message>() {
				@Override
				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
			};
			messagesList.setSelectionModel(new DefaultListSelectionModel() {
				@Override
				public void setSelectionInterval(int index0, int index1) {
					super.setSelectionInterval(-1, -1);
				}
			});
			messagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			messagesList.setCellRenderer(new MessageCellRenderer(appClient.getThisUser().getUsername()));
			messagesList.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					// force cache invalidation by temporarily setting fixed height
					messagesList.setFixedCellHeight(10);
					messagesList.setFixedCellHeight(-1);
				}
			});
			messagesScrollPane.setViewportView(messagesList);
		}

		private void show() {
			messagesList.setListData(conversation.getMessages().toArray(new Message[0]));
			contentPane.add(messagesScrollPane, gbc_scrollPane_1);

			contentPane.revalidate();
			contentPane.repaint();
		}

		private void hide() {
			contentPane.remove(messagesScrollPane);

			contentPane.revalidate();
			contentPane.repaint();
		}
	}
}
