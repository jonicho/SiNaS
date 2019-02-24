package de.sinas.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import de.sinas.Message;

public class MessageCellRenderer implements ListCellRenderer<Message> {
    private final JPanel panel;
    private final JLabel senderLabel = new JLabel();
    private final JTextArea textArea = new JTextArea();
    private final JLabel dateLabel = new JLabel();
    private final String ownUsername;
    private String lastMessageId = null;

    public MessageCellRenderer(String ownUsername) {
        this.ownUsername = ownUsername;
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(10);
        panel = new JPanel(borderLayout);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        panel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5))));
        panel.add(senderLabel, BorderLayout.NORTH);
        panel.add(textArea, BorderLayout.CENTER);
        panel.add(dateLabel, BorderLayout.SOUTH);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message message, int index, boolean isSelected, boolean cellHasFocus) {
    	if (message.getId().equals(lastMessageId)) {
    		return panel;
    	}
    	
        boolean isOwnMessage = message.getSender().equals(ownUsername);
        
        panel.setBackground(list.getBackground());
        
        senderLabel.setText(message.getSender());
        textArea.setText(message.getContent());
        dateLabel.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(message.getTimestamp())));

        senderLabel.setHorizontalAlignment(isOwnMessage ? JLabel.RIGHT : JLabel.LEFT);
        textArea.setComponentOrientation(isOwnMessage ? ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT);
        dateLabel.setHorizontalAlignment(isOwnMessage ? JLabel.RIGHT : JLabel.LEFT);
        
        if (list.getWidth() > 0) {
            panel.setSize(list.getWidth(), Integer.MAX_VALUE);
        }
        textArea.setSize(textArea.getPreferredSize());
        lastMessageId = message.getId();
        return panel;
    }

}