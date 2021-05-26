package com.bham.fsd.assignments.jabberserver;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class JabberServer {

	private static final int PORT = 44444;
	
	public static void main(String[] args) {
		try {
			/* Server listening on port 44444 */
			ServerSocket serverSocket = new ServerSocket(PORT);
			serverSocket.setReuseAddress(true);
			System.out.println("Server waiting for connection...");
			
			/* Accept the connection, call the client handler which defines what the thread will do, and start the Thread */
			while(true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client info: " + clientSocket.getInetAddress().getCanonicalHostName());
				
				ClientConnection client = new ClientConnection(clientSocket);
				new Thread(client).start();
			}
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
}