package de.sinas.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

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
import javax.swing.border.EmptyBorder;

import de.sinas.Conversation;
import de.sinas.client.AppClient;
import de.sinas.client.gui.language.Language;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GUI extends JFrame {
	private JPanel contentPane;
	private JTextField messageTextField;
	private JPanel conversationPanel;
	private JList<GUIConversation> conversationsList;
	private Language lang;
	private AppClient appClient;

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
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(100, 100));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);

		conversationsList = new JList<GUIConversation>();
		conversationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(conversationsList);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 1;
		contentPane.add(scrollPane_1, gbc_scrollPane_1);

		conversationPanel = new JPanel();
		scrollPane_1.setViewportView(conversationPanel);

		JMenuBar menuBar = new JMenuBar();
		GridBagConstraints gbc_menuBar = new GridBagConstraints();
		gbc_menuBar.anchor = GridBagConstraints.WEST;
		gbc_menuBar.gridwidth = 3;
		gbc_menuBar.insets = new Insets(0, 0, 5, 5);
		gbc_menuBar.gridx = 0;
		gbc_menuBar.gridy = 0;
		contentPane.add(menuBar, gbc_menuBar);
		
		JMenu mnOptions = new JMenu(lang.getString("options"));
		menuBar.add(mnOptions);
		
		JMenuItem mntmAddConversation = new JMenuItem(lang.getString("add_conversation"));
		mntmAddConversation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onAddConversation();
			}
		});
		mnOptions.add(mntmAddConversation);

		JMenu mnHelp = new JMenu(lang.getString("help"));
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem(lang.getString("about"));
		mnHelp.add(mntmAbout);

		messageTextField = new JTextField();
		GridBagConstraints gbc_messageTextField = new GridBagConstraints();
		gbc_messageTextField.insets = new Insets(0, 0, 0, 5);
		gbc_messageTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_messageTextField.gridx = 1;
		gbc_messageTextField.gridy = 2;
		contentPane.add(messageTextField, gbc_messageTextField);
		messageTextField.setColumns(10);

		JButton sendButton = new JButton(lang.getString("send"));
		GridBagConstraints gbc_sendButton = new GridBagConstraints();
		gbc_sendButton.gridx = 2;
		gbc_sendButton.gridy = 2;
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

	private void createUpdateListener() {
		appClient.addUpdateListener(() -> {
			conversationsList.setListData(appClient.getConversations().stream().map(c -> new GUIConversation(c)).toArray(GUIConversation[]::new));
		});
	}

	private void createErrorListener() {
		appClient.addErrorListener(errorCode -> {
			JOptionPane.showMessageDialog(this, lang.getString("error_code") + ": " + errorCode, lang.getString("some_error_occurred"), JOptionPane.ERROR_MESSAGE);
		});
	}

	private class GUIConversation {
		private final Conversation conversation;

		public GUIConversation(Conversation conversation) {
			this.conversation = conversation;
		}

		public Conversation getConversation() {
			return conversation;
		}

		@Override
		public String toString() {
			return conversation.getHTMLSummary();
		}
	}

}
