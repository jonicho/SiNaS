package de.sinas.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import de.sinas.crypto.*;

/**
 * <p>
 * Materialien zu den zentralen NRW-Abiturpruefungen im Fach Informatik ab 2018
 * </p>
 * <p>
 * Klasse Client
 * </p>
 * <p>
 * Objekte von Unterklassen der abstrakten Klasse Client ermoeglichen
 * Netzwerkverbindungen zu einem Server mittels TCP/IP-Protokoll. Nach
 * Verbindungsaufbau koennen Zeichenketten (Strings) zum Server gesendet und von
 * diesem empfangen werden, wobei der Nachrichtenempfang nebenlaeufig geschieht.
 * Zur Vereinfachung finden Nachrichtenversand und -empfang zeilenweise statt,
 * d. h., beim Senden einer Zeichenkette wird ein Zeilentrenner ergaenzt und
 * beim Empfang wird dieser entfernt. Jede empfangene Nachricht wird einer
 * Ereignisbehandlungsmethode uebergeben, die in Unterklassen implementiert
 * werden muss. Es findet nur eine rudimentaere Fehlerbehandlung statt, so dass
 * z.B. Verbindungsabbrueche nicht zu einem Programmabbruch fuehren. Eine einmal
 * unterbrochene oder getrennte Verbindung kann nicht reaktiviert werden.
 * </p>
 * 
 * @author Qualitaets- und UnterstuetzungsAgentur - Landesinstitut fuer Schule
 * @version 30.08.2016
 */

public abstract class Client {
	private MessageHandler messageHandler;
	private AESHandler hAES = new AESHandler();
	private RSAHandler hRSA = new RSAHandler();
	private HashHandler hasher = new HashHandler();

	private class MessageHandler extends Thread {
		private SocketWrapper socketWrapper;
		private boolean active;

		private class SocketWrapper {
			private Socket socket;
			private BufferedReader fromServer;
			private PrintWriter toServer;

			public SocketWrapper(String pServerIP, int pServerPort) {
				try {
					socket = new Socket(pServerIP, pServerPort);
					toServer = new PrintWriter(socket.getOutputStream(), true);
					fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
					socket = null;
					toServer = null;
					fromServer = null;
				}
			}

			public String receive() throws IOException {
				if (fromServer != null)
					return fromServer.readLine();
				return (null);
			}

			public void send(String pMessage) {
				if (toServer != null) {
					toServer.println(pMessage);
				}
			}

			public void close() {
				if (socket != null)
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		private MessageHandler(String pServerIP, int pServerPort) {
			socketWrapper = new SocketWrapper(pServerIP, pServerPort);
			start();
			if (socketWrapper.socket != null)
				active = true;
		}

		@Override
		public void run() {
			String message = null;
			while (active) {
				try {
					message = socketWrapper.receive();
				} catch (IOException e) {
					e.printStackTrace();
					close();
					connectionLost();
				}
				if (message != null)
					processMessage(message);
				else {
					close();
					connectionLost();
				}
			}
		}

		private void send(String pMessage) {
			if (active)
				socketWrapper.send(pMessage);
		}

		private void close() {
			if (active) {
				active = false;
				socketWrapper.close();
			}
		}
	}

	/**
	 * @return the hAES
	 */
	public AESHandler getAESHandler() {
		return hAES;
	}

	/**
	 * @return the hasher
	 */
	public HashHandler getHashHandler() {
		return hasher;
	}

	/**
	 * @return the hRSA
	 */
	public RSAHandler getRSAHandler() {
		return hRSA;
	}
	

	public Client(String pServerIP, int pServerPort) {
		messageHandler = new MessageHandler(pServerIP, pServerPort);
	}

	public boolean isConnected() {
		return (messageHandler.active);
	}

	public void send(String pMessage) {
		messageHandler.send(pMessage);
	}

	public void close() {
		messageHandler.close();
	}

	public abstract void processMessage(String pMessage);
	
	public abstract void connectionLost();

}
