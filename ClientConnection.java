package com.bham.fsd.assignments.jabberserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable{
	private JabberDatabase db;
	private String username;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	public ClientConnection(Socket clientSocket) throws IOException {
		this.ois = new ObjectInputStream(clientSocket.getInputStream());
		this.oos = new ObjectOutputStream(clientSocket.getOutputStream());
		this.db = new JabberDatabase();
	}

	@Override
	public void run() {
		try {
			Boolean isListening = true;
			JabberMessage message;

			while(((message = (JabberMessage) ois.readObject()) != null) && isListening) {
				String clientMessage = message.getMessage();
				System.out.println(clientMessage);
				if(clientMessage.startsWith("signin")) {
					handleLogin(clientMessage);
				}
				else if(clientMessage.startsWith("register")) {
					handleRegister(clientMessage);
				}
				else if(clientMessage.startsWith("timeline")) {
					handleTimeline();
				}
				else if (clientMessage.startsWith("like")) {
			        addLike(clientMessage);
				}
				else if(clientMessage.startsWith("users")) {
					whoToFollow(clientMessage);
				}
				else if(clientMessage.startsWith("follow")) {
					addFollow(clientMessage);
				}
				else if(clientMessage.startsWith("post")) {
					addPost(clientMessage);
				}
			}
		}
		catch (IOException | ClassNotFoundException e) {
			System.out.println(e);
		}
	}
	
	public void addPost(String clientMessage) {
		try {
			String jabText = clientMessage.substring(5, clientMessage.length());
			db.addJab(username, jabText);
			JabberMessage serverMessage = new JabberMessage("posted");
			oos.writeObject(serverMessage);
			oos.flush();
			
			/* Re-render timeline to reflect change */ 
			handleTimeline(); 
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public void addLike(String clientMessage) {
		try {
			int jabID = Integer.parseInt(clientMessage.substring(5, clientMessage.length()));
			db.addLike(db.getUserID(username), jabID);
			JabberMessage serverMessage = new JabberMessage("posted");
			oos.writeObject(serverMessage);
			oos.flush();
			
			/* Re-render timeline to reflect change */ 
			handleTimeline(); 
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public void whoToFollow(String clientMessage) {
		try {
			ArrayList<ArrayList<String>> data;
			data = db.getUsersNotFollowed(db.getUserID(username));
			JabberMessage serverMessage = new JabberMessage("users", data);
			oos.writeObject(serverMessage);
			oos.flush();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public void addFollow(String clientMessage) {
		try {
			String userToBeFollowed = clientMessage.substring(7, clientMessage.length());
			db.addFollower(db.getUserID(username), userToBeFollowed);
			JabberMessage serverMessage = new JabberMessage("posted");
			oos.writeObject(serverMessage);
			oos.flush();
			
			/* Re-render timeline to reflect change */ 
			handleTimeline();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}

	public void handleTimeline() {
		try {
			ArrayList<ArrayList<String>> data = db.getTimelineOfUserEx(username);
			JabberMessage serverMessage = new JabberMessage("timeline", data);
			oos.writeObject(serverMessage);
			oos.flush();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public void handleLogin(String clientMessage) {
		try {
			/* Check the username against the database */
			username = clientMessage.substring(7, clientMessage.length());
			int result = db.getUserID(username);
		
			/* Return different server messages depending on the result of the query */
			if(result == -1) {
				JabberMessage serverMessage = new JabberMessage("unknown-user");
				oos.writeObject(serverMessage);
				oos.flush();
			}
			else {
				JabberMessage serverMessage = new JabberMessage("signedin");
				oos.writeObject(serverMessage);
				oos.flush();
				//serverTimeline();
			}
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public void handleRegister(String clientMessage) {
		try {
			username = clientMessage.substring(9, clientMessage.length());
			System.out.println(username);
			db.addUser(username, username + "@gmail.com"); // change to email address of user
			
			JabberMessage serverMessage = new JabberMessage("signedin");
			oos.writeObject(serverMessage);
			oos.flush();
			//serverTimeline();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
}