package de.sinas.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Client {
	private MessageHandler messageHandler;

	public Client(String serverIP, int serverPort) {
		messageHandler = new MessageHandler(serverIP, serverPort);
	}

	public boolean isConnected() {
		return messageHandler.active;
	}

	protected void send(String message) {
		messageHandler.send(message);
	}

	public void close() {
		messageHandler.close();
	}

	protected abstract void processMessage(String message);

	protected abstract void connectionLost();

	private class MessageHandler extends Thread {
		private SocketWrapper socketWrapper;
		private boolean active;

		private MessageHandler(String serverIP, int serverPort) {
			socketWrapper = new SocketWrapper(serverIP, serverPort);
			active = socketWrapper.socket != null;
			start();
		}

		@Override
		public void run() {
			while (active) {
				String message = socketWrapper.receive();
				if (message == null) {
					break;
				}
				processMessage(message);
			}
			close();
			connectionLost();
		}

		private void send(String message) {
			if (active) {
				socketWrapper.send(message);
			}
		}

		private void close() {
			if (active) {
				active = false;
				socketWrapper.close();
			}
		}

		private class SocketWrapper {
			private Socket socket;
			private BufferedReader fromServer;
			private PrintWriter toServer;

			public SocketWrapper(String serverIP, int serverPort) {
				try {
					socket = new Socket(serverIP, serverPort);
					toServer = new PrintWriter(socket.getOutputStream(), true);
					fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
					socket = null;
					toServer = null;
					fromServer = null;
				}
			}

			public String receive() {
				if (fromServer == null) {
					return null;
				}
				try {
					return fromServer.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			public void send(String pMessage) {
				if (toServer != null) {
					toServer.println(pMessage);
				}
			}

			public void close() {
				if (socket == null) {
					return;
				}
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
