package de.sinas.client.gui;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;

import javafx.application.Platform;

public class SiNaSTrayIcon {
    private Gui gui;

    public SiNaSTrayIcon(Gui gui, Runnable onTrayIconClicked) {
        this.gui = gui;
        try {
            TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit()
                    .getImage("http://icons.iconarchive.com/icons/paomedia/small-n-flat/256/sign-check-icon.png")); // TODO: replace place holder icon
            trayIcon.addActionListener((e) -> {
                Platform.runLater(onTrayIconClicked);
            });
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
