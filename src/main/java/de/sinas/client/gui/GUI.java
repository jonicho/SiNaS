package de.sinas.client.gui;

import de.sinas.Conversation;
import de.sinas.client.AppClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUI extends JFrame {
	private JPanel contentPane;
	private JList<GUIConversation> conversationsList;

	public GUI(AppClient appClient) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		conversationsList = new JList<>();
		contentPane.add(conversationsList, BorderLayout.WEST);
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
