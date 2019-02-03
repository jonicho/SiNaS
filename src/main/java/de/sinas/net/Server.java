package de.sinas.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Server {
	private NewConnectionHandler connectionHandler;
	private List<ClientMessageHandler> messageHandlers;

	public Server(int port) {
		connectionHandler = new NewConnectionHandler(port);
		messageHandlers = new ArrayList<ClientMessageHandler>();
	}

	public boolean isOpen() {
		return connectionHandler.active;
	}

	public boolean isConnectedTo(String clientIP, int clientPort) {
		ClientMessageHandler messageHandler = getClientMessageHandler(clientIP, clientPort);
		return messageHandler != null && messageHandler.active;
	}

	public void send(String clientIP, int clientPort, String message) {
		ClientMessageHandler messageHandler = getClientMessageHandler(clientIP, clientPort);
		if (messageHandler == null) {
			return;
		}
		messageHandler.send(message);
	}

	public void sendToAll(String message) {
		synchronized (messageHandlers) {
			for (ClientMessageHandler messageHandler : messageHandlers) {
				messageHandler.send(message);
			}
		}
	}

	public void closeConnection(String clientIP, int clientPort) {
		ClientMessageHandler messageHandler = getClientMessageHandler(clientIP, clientPort);
		if (messageHandler == null) {
			return;
		}
		processClosingConnection(clientIP, clientPort);
		messageHandler.close();
		removeClientMessageHandler(messageHandler);

	}

	public void close() {
		connectionHandler.close();

		synchronized (messageHandlers) {
			for (ClientMessageHandler messageHandler : messageHandlers) {
				processClosingConnection(messageHandler.getClientIP(), messageHandler.getClientPort());
				messageHandler.close();
			}
			messageHandlers.clear();
		}

	}

	private void addClientMessageHandler(ClientMessageHandler clientMessageHandler) {
		synchronized (messageHandlers) {
			messageHandlers.add(clientMessageHandler);
		}
	}

	private void removeClientMessageHandler(ClientMessageHandler clientMessageHandler) {
		synchronized (messageHandlers) {
			messageHandlers.remove(clientMessageHandler);
		}
	}

	private ClientMessageHandler getClientMessageHandler(String clientIP, int clientPort) {
		synchronized (messageHandlers) {
			for (ClientMessageHandler messageHandler : messageHandlers) {
				if (messageHandler.getClientPort() == clientPort && messageHandler.getClientIP().equals(clientIP)) {
					return messageHandler;
				}
			}
			return null;
		}
	}

	public abstract void processNewConnection(String clientIP, int clientPort);

	public abstract void processMessage(String clientIP, int clientPort, String message);

	public abstract void processClosingConnection(String clientIP, int clientPort);

	private class NewConnectionHandler extends Thread {
		private ServerSocket serverSocket;
		private boolean active;

		public NewConnectionHandler(int port) {
			try {
				serverSocket = new ServerSocket(port);
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
					addClientMessageHandler(new ClientMessageHandler(clientSocket));
					processNewConnection(clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void close() {
			active = false;
			if (serverSocket == null) {
				return;
			}
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

		private ClientMessageHandler(Socket clientSocket) {
			socketWrapper = new ClientSocketWrapper(clientSocket);
			if (clientSocket != null) {
				start();
				active = true;
			} else {
				active = false;
			}
		}

		@Override
		public void run() {
			while (active) {
				String message = socketWrapper.receive();
				if (message == null) {
					break;
				}
				processMessage(socketWrapper.getClientIP(), socketWrapper.getClientPort(), message);
			}

			ClientMessageHandler messageHandler = getClientMessageHandler(socketWrapper.getClientIP(),
					socketWrapper.getClientPort());
			if (messageHandler == null) {
				return;
			}
			messageHandler.close();
			removeClientMessageHandler(messageHandler);
			processClosingConnection(socketWrapper.getClientIP(), socketWrapper.getClientPort());
		}

		public void send(String message) {
			if (active) {
				socketWrapper.send(message);
			}
		}

		public void close() {
			if (active) {
				active = false;
				socketWrapper.close();
			}
		}

		public String getClientIP() {
			return socketWrapper.getClientIP();
		}

		public int getClientPort() {
			return socketWrapper.getClientPort();
		}

		private class ClientSocketWrapper {
			private Socket clientSocket;
			private BufferedReader fromClient;
			private PrintWriter toClient;

			public ClientSocketWrapper(Socket socket) {
				try {
					clientSocket = socket;
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
				if (fromClient == null) {
					return null;
				}
				try {
					return fromClient.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			public void send(String message) {
				if (toClient != null) {
					toClient.println(message);
				}
			}

			public String getClientIP() {
				if (clientSocket == null) {
					return null;
				}
				return clientSocket.getInetAddress().getHostAddress();
			}

			public int getClientPort() {
				if (clientSocket == null) {
					return 0;
				}
				return clientSocket.getPort();
			}

			public void close() {
				if (clientSocket == null) {
					return;
				}
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}