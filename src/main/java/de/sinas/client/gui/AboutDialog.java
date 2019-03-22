package de.sinas.client.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import de.sinas.client.gui.language.Language;

public class AboutDialog extends JDialog {
	private final JPanel contentPanel = new JPanel();
	private final String githubLink = "https://github.com/JonasRKeller/SiNaS";

	public AboutDialog(Frame owner, Language lang) {
		super(owner, ModalityType.APPLICATION_MODAL);
		setTitle("SiNaS - " + lang.getString("about"));
		setBounds(100, 100, 450, 300);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 440, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblSinas = new JLabel("SiNaS");
			lblSinas.setFont(lblSinas.getFont().deriveFont(40.0f));
			lblSinas.setHorizontalAlignment(SwingConstants.CENTER);
			GridBagConstraints gbc_lblSinas = new GridBagConstraints();
			gbc_lblSinas.fill = GridBagConstraints.BOTH;
			gbc_lblSinas.insets = new Insets(0, 0, 5, 0);
			gbc_lblSinas.gridx = 0;
			gbc_lblSinas.gridy = 0;
			contentPanel.add(lblSinas, gbc_lblSinas);
		}
		{
			JLabel lblCreatedBy = new JLabel(lang.getString("developed_by") + ":");
			GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
			gbc_lblCreatedBy.fill = GridBagConstraints.VERTICAL;
			gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 0);
			gbc_lblCreatedBy.gridx = 0;
			gbc_lblCreatedBy.gridy = 2;
			contentPanel.add(lblCreatedBy, gbc_lblCreatedBy);
		}
		{
			JLabel lblYoshiExeler = new JLabel("Yoshi Exeler");
			GridBagConstraints gbc_lblYoshiExeler = new GridBagConstraints();
			gbc_lblYoshiExeler.insets = new Insets(0, 0, 5, 0);
			gbc_lblYoshiExeler.fill = GridBagConstraints.VERTICAL;
			gbc_lblYoshiExeler.gridx = 0;
			gbc_lblYoshiExeler.gridy = 3;
			contentPanel.add(lblYoshiExeler, gbc_lblYoshiExeler);
		}
		{
			JLabel lblJonasKeller = new JLabel("Jonas Keller");
			GridBagConstraints gbc_lblJonasKeller = new GridBagConstraints();
			gbc_lblJonasKeller.fill = GridBagConstraints.VERTICAL;
			gbc_lblJonasKeller.insets = new Insets(0, 0, 5, 0);
			gbc_lblJonasKeller.gridx = 0;
			gbc_lblJonasKeller.gridy = 4;
			contentPanel.add(lblJonasKeller, gbc_lblJonasKeller);
		}
		{
			JLabel lblGithub = new JLabel("GitHub:");
			GridBagConstraints gbc_lblGithub = new GridBagConstraints();
			gbc_lblGithub.insets = new Insets(0, 0, 5, 0);
			gbc_lblGithub.gridx = 0;
			gbc_lblGithub.gridy = 6;
			contentPanel.add(lblGithub, gbc_lblGithub);
		}
		{
			JLabel lblGithublink = new JLabel("<html><a href=\"" + githubLink + "\">" + githubLink + "</a></html>");
			lblGithublink.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					onGitHubLink();
				}
			});
			lblGithublink.setCursor(new Cursor(Cursor.HAND_CURSOR));
			GridBagConstraints gbc_lblGithublink = new GridBagConstraints();
			gbc_lblGithublink.gridx = 0;
			gbc_lblGithublink.gridy = 7;
			contentPanel.add(lblGithublink, gbc_lblGithublink);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton(lang.getString("ok"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						onOKButton();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	private void onGitHubLink() {
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			return;
		}
		SwingUtilities.invokeLater(() -> {
			try {
				Desktop.getDesktop().browse(new URI(githubLink));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void onOKButton() {
		dispose();
	}
}
