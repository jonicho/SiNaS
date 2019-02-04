package de.sinas.client.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.sinas.Conversation;
import de.sinas.client.AppClient;
import de.sinas.client.gui.language.Language;

public class ConversationEditDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private JTextField nameTextField;
	private JTextField usernameSearchTextField;
	private String[] userSearchResult = {};
	private JList<String> usersSearchList;
	private JList<String> usersList;

	private AppClient appClient;
	private boolean save;

	public ConversationEditDialog(Frame owner, AppClient appClient, Conversation conversation, Language lang) {
		super(owner, ModalityType.APPLICATION_MODAL);
		this.appClient = appClient;
		setBounds(100, 100, 450, 494);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0 };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblName = new JLabel(lang.getString("name"));
			GridBagConstraints gbc_lblName = new GridBagConstraints();
			gbc_lblName.insets = new Insets(0, 0, 5, 5);
			gbc_lblName.anchor = GridBagConstraints.EAST;
			gbc_lblName.gridx = 0;
			gbc_lblName.gridy = 0;
			contentPanel.add(lblName, gbc_lblName);
		}
		{
			nameTextField = new JTextField(conversation.getName());
			GridBagConstraints gbc_nameTextField = new GridBagConstraints();
			gbc_nameTextField.insets = new Insets(0, 0, 5, 0);
			gbc_nameTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_nameTextField.gridx = 1;
			gbc_nameTextField.gridy = 0;
			contentPanel.add(nameTextField, gbc_nameTextField);
			nameTextField.setColumns(10);
		}
		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.insets = new Insets(0, 0, 5, 0);
			gbc_separator.fill = GridBagConstraints.BOTH;
			gbc_separator.gridwidth = 2;
			gbc_separator.gridx = 0;
			gbc_separator.gridy = 1;
			contentPanel.add(separator, gbc_separator);
		}
		{
			JLabel lblSearch = new JLabel(lang.getString("search"));
			GridBagConstraints gbc_lblSearch = new GridBagConstraints();
			gbc_lblSearch.anchor = GridBagConstraints.EAST;
			gbc_lblSearch.insets = new Insets(0, 0, 5, 5);
			gbc_lblSearch.gridx = 0;
			gbc_lblSearch.gridy = 2;
			contentPanel.add(lblSearch, gbc_lblSearch);
		}
		{
			usernameSearchTextField = new JTextField();
			usernameSearchTextField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					onUsernameSearch();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					onUsernameSearch();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					onUsernameSearch();
				}
			});
			GridBagConstraints gbc_usernameSearchTextField = new GridBagConstraints();
			gbc_usernameSearchTextField.insets = new Insets(0, 0, 5, 0);
			gbc_usernameSearchTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_usernameSearchTextField.gridx = 1;
			gbc_usernameSearchTextField.gridy = 2;
			contentPanel.add(usernameSearchTextField, gbc_usernameSearchTextField);
			usernameSearchTextField.setColumns(10);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 3;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				usersSearchList = new JList<>();
				usersSearchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				scrollPane.setViewportView(usersSearchList);
			}
		}
		{
			JButton btnAddUser = new JButton(lang.getString("add_user"));
			btnAddUser.addActionListener(e -> onAddUser());
			GridBagConstraints gbc_btnAddUser = new GridBagConstraints();
			gbc_btnAddUser.anchor = GridBagConstraints.EAST;
			gbc_btnAddUser.insets = new Insets(0, 0, 5, 0);
			gbc_btnAddUser.gridx = 1;
			gbc_btnAddUser.gridy = 4;
			contentPanel.add(btnAddUser, gbc_btnAddUser);
		}
		{
			JSeparator separator = new JSeparator();
			GridBagConstraints gbc_separator = new GridBagConstraints();
			gbc_separator.fill = GridBagConstraints.BOTH;
			gbc_separator.gridwidth = 2;
			gbc_separator.insets = new Insets(0, 0, 5, 0);
			gbc_separator.gridx = 0;
			gbc_separator.gridy = 5;
			contentPanel.add(separator, gbc_separator);
		}
		{
			JLabel lblUsers = new JLabel(lang.getString("users"));
			GridBagConstraints gbc_lblUsers = new GridBagConstraints();
			gbc_lblUsers.insets = new Insets(0, 0, 5, 5);
			gbc_lblUsers.gridx = 0;
			gbc_lblUsers.gridy = 6;
			contentPanel.add(lblUsers, gbc_lblUsers);
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			GridBagConstraints gbc_scrollPane = new GridBagConstraints();
			gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
			gbc_scrollPane.fill = GridBagConstraints.BOTH;
			gbc_scrollPane.gridx = 1;
			gbc_scrollPane.gridy = 6;
			contentPanel.add(scrollPane, gbc_scrollPane);
			{
				usersList = new JList<>();
				DefaultListModel<String> defaultListModel = new DefaultListModel<>();
				defaultListModel.addAll(conversation.getUsers());
				usersList.setModel(defaultListModel);
				usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				scrollPane.setViewportView(usersList);
			}
		}
		{
			JButton btnRemoveUser = new JButton(lang.getString("remove_user"));
			btnRemoveUser.addActionListener(e -> onRemoveUser());
			GridBagConstraints gbc_btnRemoveUser = new GridBagConstraints();
			gbc_btnRemoveUser.anchor = GridBagConstraints.EAST;
			gbc_btnRemoveUser.gridx = 1;
			gbc_btnRemoveUser.gridy = 7;
			contentPanel.add(btnRemoveUser, gbc_btnRemoveUser);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton saveButton = new JButton(lang.getString("save"));
				saveButton.addActionListener(e -> onSave());
				buttonPane.add(saveButton);
				getRootPane().setDefaultButton(saveButton);
			}
			{
				JButton cancelButton = new JButton(lang.getString("cancel"));
				cancelButton.addActionListener(e -> onCancel());
				buttonPane.add(cancelButton);
			}
		}

		appClient.setSearchResultListener((query, results) -> {
			if (usernameSearchTextField.getText().equals(query)) {
				userSearchResult = results;
				updateUsersSearchList();
			}
		});

		appClient.searchUser("");
	}

	private void updateUsersSearchList() {
		List<String> resultsList = new ArrayList<>(Arrays.asList(userSearchResult));
		resultsList.removeAll(Arrays.asList(getUsers()));
		usersSearchList.setListData(resultsList.toArray(String[]::new));
	}

	private void onUsernameSearch() {
		appClient.searchUser(usernameSearchTextField.getText());
	}

	private void onAddUser() {
		String user = usersSearchList.getSelectedValue();
		if (user == null) {
			return;
		}
		((DefaultListModel<String>) usersList.getModel()).addElement(user);
		updateUsersSearchList();
	}

	private void onRemoveUser() {
		String user = usersList.getSelectedValue();
		if (user == null) {
			return;
		}
		((DefaultListModel<String>) usersList.getModel()).removeElement(user);
		updateUsersSearchList();
	}

	private void onSave() {
		save = true;
		dispose();
	}

	private void onCancel() {
		save = false;
		dispose();
	}

	public String getConversationName() {
		return nameTextField.getText();
	}

	public String[] getUsers() {
		String[] result = new String[usersList.getModel().getSize()];
		for (int i = 0; i < usersList.getModel().getSize(); i++) {
			result[i] = usersList.getModel().getElementAt(i);
		}
		return result;
	}

	public boolean showDialog() {
		setVisible(true);
		return save;
	}
}
