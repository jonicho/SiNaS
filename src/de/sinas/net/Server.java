package de.sinas.net;

import java.net.*;

import de.sinas.crypto.AESHandler;

import java.io.*;

import de.sinas.crypto.*;

/**
 * <p>
 * Materialien zu den zentralen NRW-Abiturpruefungen im Fach Informatik ab 2018
 * </p>
 * <p>
 * Klasse Server
 * </p>
 * <p>
 * Objekte von Unterklassen der abstrakten Klasse Server ermoeglichen das
 * Anbieten von Serverdiensten, so dass Clients Verbindungen zum Server mittels
 * TCP/IP-Protokoll aufbauen koennen. Zur Vereinfachung finden Nachrichtenversand
 * und -empfang zeilenweise statt, d. h., beim Senden einer Zeichenkette wird ein
 * Zeilentrenner ergaenzt und beim Empfang wird dieser entfernt.
 * Verbindungsannahme, Nachrichtenempfang und Verbindungsende geschehen
 * nebenlaeufig. Auf diese Ereignisse muss durch Ueberschreiben der entsprechenden
 * Ereignisbehandlungsmethoden reagiert werden. Es findet nur eine rudimentaere
 * Fehlerbehandlung statt, so dass z.B. Verbindungsabbrueche nicht zu einem
 * Programmabbruch fuehren. Einmal unterbrochene oder getrennte Verbindungen
 * koennen nicht reaktiviert werden.
  * </p>
 *
 * @author Qualitaets- und UnterstuetzungsAgentur - Landesinstitut fuer Schule
 * @version 30.08.2016
 */
public abstract class Server {
	private NewConnectionHandler connectionHandler;
	private List<ClientMessageHandler> messageHandlers;
	private AESHandler hAES = new AESHandler();
	private RSAHandler hRSA = new RSAHandler();
	private HashHandler hasher = new HashHandler();

	private class NewConnectionHandler extends Thread {
		private ServerSocket serverSocket;
		private boolean active;

		public NewConnectionHandler(int pPort) {
			try {
				serverSocket = new ServerSocket(pPort);
				start();
				active = true;
			} catch (Exception e) {
				e.printStackTrace();
				serverSocket = null;
				active = false;
			}
		}

		@Override
		public void run() {
			while (active) {
				try {
					Socket clientSocket = serverSocket.accept();
					addNewClientMessageHandler(clientSocket);
					processNewConnection(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void close() {
			active = false;
			if (serverSocket != null)
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private class ClientMessageHandler extends Thread {
		private ClientSocketWrapper socketWrapper;
		private boolean active;

		private class ClientSocketWrapper {
			private Socket clientSocket;
			private BufferedReader fromClient;
			private PrintWriter toClient;

			public ClientSocketWrapper(Socket pSocket) {
				try {
					clientSocket = pSocket;
					toClient = new PrintWriter(clientSocket.getOutputStream(), true);
					fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
					clientSocket = null;
					toClient = null;
					fromClient = null;
				}
			}

			public String receive() {
				if (fromClient != null)
					try {
						return fromClient.readLine();
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
				return (null);
			}

			public void send(String pMessage) {
				if (toClient != null) {
					toClient.println(pMessage);
				}
			}

			public String getClientIP() {
				if (clientSocket != null)
					return (clientSocket.getInetAddress().getHostAddress());
				else
					return (null);
			}

			public int getClientPort() {
				if (clientSocket != null)
					return (clientSocket.getPort());
				else
					return (0);
			}

			public void close() {
				if (clientSocket != null)
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}

		private ClientMessageHandler(Socket pClientSocket) {
			socketWrapper = new ClientSocketWrapper(pClientSocket);
			if (pClientSocket != null) {
				start();
				active = true;
			} else {
				active = false;
			}
		}

		@Override
		public void run() {
			String message = null;
			while (active) {
				message = socketWrapper.receive();
				if (message != null)
					processMessage(socketWrapper.getClientIP(), socketWrapper.getClientPort(), message);
				else {
					ClientMessageHandler aMessageHandler = findClientMessageHandler(socketWrapper.getClientIP(),
							socketWrapper.getClientPort());
					if (aMessageHandler != null) {
						aMessageHandler.close();
						removeClientMessageHandler(aMessageHandler);
						processClosingConnection(socketWrapper.getClientIP(), socketWrapper.getClientPort());
					}
				}
			}
		}

		public void send(String pMessage) {
			if (active)
				socketWrapper.send(pMessage);
		}

		public void close() {
			if (active) {
				active = false;
				socketWrapper.close();
			}
		}

		public String getClientIP() {
			return (socketWrapper.getClientIP());
		}

		public int getClientPort() {
			return (socketWrapper.getClientPort());
		}

	}

	public Server(int pPort) {
		connectionHandler = new NewConnectionHandler(pPort);
		messageHandlers = new List<ClientMessageHandler>();
	}

	/**
	 * @return the hAES
	 */
	public AESHandler gethAES() {
		return hAES;
	}

	/**
	 * @return the hasher
	 */
	public HashHandler getHasher() {
		return hasher;
	}

	/**
	 * @return the hRSA
	 */
	public RSAHandler gethRSA() {
		return hRSA;
	}

	public boolean isOpen() {
		return (connectionHandler.active);
	}

	public boolean isConnectedTo(String pClientIP, int pClientPort) {
		ClientMessageHandler aMessageHandler = findClientMessageHandler(pClientIP, pClientPort);
		if (aMessageHandler != null)
			return (aMessageHandler.active);
		else
			return (false);
	}

	public void send(String pClientIP, int pClientPort, String pMessage) {
		ClientMessageHandler aMessageHandler = this.findClientMessageHandler(pClientIP, pClientPort);
		if (aMessageHandler != null)
			aMessageHandler.send(pMessage);
	}

	public void sendToAll(String pMessage) {
		synchronized (messageHandlers) {
			messageHandlers.toFirst();
			while (messageHandlers.hasAccess()) {
				messageHandlers.getContent().send(pMessage);
				messageHandlers.next();
			}
		}
	}

	public void closeConnection(String pClientIP, int pClientPort) {
		ClientMessageHandler aMessageHandler = findClientMessageHandler(pClientIP, pClientPort);
		if (aMessageHandler != null) {
			processClosingConnection(pClientIP, pClientPort);
			aMessageHandler.close();
			removeClientMessageHandler(aMessageHandler);
		}

	}

	public void close() {
		connectionHandler.close();

		synchronized (messageHandlers) {
			ClientMessageHandler aMessageHandler;
			messageHandlers.toFirst();
			while (messageHandlers.hasAccess()) {
				aMessageHandler = messageHandlers.getContent();
				processClosingConnection(aMessageHandler.getClientIP(), aMessageHandler.getClientPort());
				aMessageHandler.close();
				messageHandlers.remove();
			}
		}

	}

	public abstract void processNewConnection(String pClientIP, int pClientPort);

	public abstract void processMessage(String pClientIP, int pClientPort, String pMessage);

	public abstract void processClosingConnection(String pClientIP, int pClientPort);

	private void addNewClientMessageHandler(Socket pClientSocket) {
		synchronized (messageHandlers) {
			messageHandlers.append(new Server.ClientMessageHandler(pClientSocket));
		}
	}

	private void removeClientMessageHandler(ClientMessageHandler pClientMessageHandler) {
		synchronized (messageHandlers) {
			messageHandlers.toFirst();
			while (messageHandlers.hasAccess()) {
				if (pClientMessageHandler == messageHandlers.getContent()) {
					messageHandlers.remove();
					return;
				} else
					messageHandlers.next();
			}
		}
	}

	private ClientMessageHandler findClientMessageHandler(String pClientIP, int pClientPort) {
		synchronized (messageHandlers) {
			ClientMessageHandler aMessageHandler;
			messageHandlers.toFirst();

			while (messageHandlers.hasAccess()) {
				aMessageHandler = messageHandlers.getContent();
				if (aMessageHandler.getClientIP().equals(pClientIP) && aMessageHandler.getClientPort() == pClientPort)
					return (aMessageHandler);
				messageHandlers.next();
			}
			return (null);
		}
	}

}