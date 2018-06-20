package de.sinas.client.gui;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import de.sinas.Message;
import de.sinas.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class ChatMessageCell extends ListCell<Message> {
    @FXML
    private Label nameLabel;
    @FXML
    private Label textLabel;
    @FXML
    private Label timeLabel;

    private ListView<Message> parentListView;
    private FXMLLoader fxmlLoader;

    private User thisUser;
    private boolean lastRight;

    public ChatMessageCell(ListView<Message> parentListView, User thisUser) {
        setFocusTraversable(false);
        this.thisUser = thisUser;
        this.parentListView = parentListView;
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);
        if (empty || message == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        boolean right = message.getSender().getUsername().equals(thisUser.getUsername());
        if (fxmlLoader == null || (right != lastRight)) {
            fxmlLoader = new FXMLLoader(getClass().getResource(
                    message.getSender().getUsername().equals(thisUser.getUsername()) ? "chatMessageRight.fxml"
                            : "chatMessageLeft.fxml"));
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lastRight = right;
        nameLabel.setText(message.getSender().getNickname());
        textLabel.setText(message.getContent());
        timeLabel.setText(new SimpleDateFormat().format(new Date(message.getTimestamp())));

        setText(null);
        setGraphic(fxmlLoader.getRoot());
        prefWidthProperty().set(0);
    }
}
