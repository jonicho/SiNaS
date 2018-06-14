package de.sinas.client.gui;

import de.sinas.Conversation;
import de.sinas.Message;
import de.sinas.User;
import de.sinas.client.AppClient;
import de.sinas.net.PROTOCOL;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Gui extends Application {
	@FXML
	private ListView<Message> chatListView;
	@FXML
	private ListView<Conversation> conversationsListView;
	private Conversation currentConversation;

	private AppClient appClient;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		appClient = new AppClient("localhost", PROTOCOL.PORT, this);
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("gui.fxml"));
		fxmlLoader.setController(this);
		Pane root = (Pane) fxmlLoader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@FXML
	public void initialize() {
		chatListView
				.setCellFactory(listView -> new ChatMessageCell(listView, new User(null, 0, "username", "nickname")));
		chatListView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> event.consume());
		conversationsListView.setCellFactory(listView -> new ListCell<Conversation>() {
			@Override
			protected void updateItem(Conversation conversation, boolean empty) {
				super.updateItem(conversation, empty);
				if (empty || conversation == null) {
					setText(null);
					setGraphic(null);
					return;
				}
				setText(conversation.getOtherUser(appClient.getThisUser()).getNickname());
				setGraphic(null);
			}
		});
	}

	@FXML
	private void onSomeButton() {
		User tu = new User("ip", 0, "username", "nickname");
		User ou = new User("ip", 0, "otherusername", "othernickname");
		Conversation c = currentConversation == null ? new Conversation("id", tu, ou) : currentConversation;
		c.addMessage(new Message("Message content", System.currentTimeMillis(), Math.random() > 0.5 ? ou : tu, false));
		setConversation(c);
	}

	private void loadMessages() {
		if (currentConversation != null)
			chatListView.setItems(FXCollections.observableArrayList(currentConversation.getMessages()));
	}

	public void setConversation(Conversation conversation) {
		this.currentConversation = conversation;
		loadMessages();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		if (appClient.isConnected())
			appClient.close();
	}
}