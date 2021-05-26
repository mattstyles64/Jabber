package com.bham.fsd.assignments.jabberserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainController implements Initializable {
	
	@FXML private Button B1;
	@FXML private TextField T1;
	@FXML private Label L1;
	@FXML private Button B2;
	@FXML private Button B3;
	private final int PORT = 44444;
	Socket clientSocket;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			clientSocket = new Socket("localhost", PORT);
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ois = new ObjectInputStream(clientSocket.getInputStream());
		} 
		catch (IOException e) {
			System.out.println(e);
		}
	}

	public void signout(ActionEvent e) throws Exception {
		clientSocket.close();
		Platform.exit();
		System.exit(0);
	}
	
	public void register(ActionEvent e) throws Exception {
		/* Send "register username" message to server */
		JabberMessage jm = new JabberMessage("register " + T1.getText());
		System.out.println(jm.getMessage());
		oos.writeObject(jm);
		oos.flush();
		
		/* Receives "signedin" message from server */
		JabberMessage response = (JabberMessage) ois.readObject();
		String message = response.getMessage();
		if(message.equals("signedin")) {
			timelinePane(e);
			suggestedFollowersPane(e);
		}
	}
	
	public void login(ActionEvent e) throws Exception {
		/* Sends "signin username" message to server */
		JabberMessage jm = new JabberMessage("signin " + T1.getText());
		oos.writeObject(jm);
		oos.flush();
		
		/* Receives "signedin" or "unknown-user" message from server */
		JabberMessage response = (JabberMessage) ois.readObject();
		String message = response.getMessage();
		if(message.equals("signedin")) {
			timelinePane(e);
			suggestedFollowersPane(e);
			
		}
		else if(message.equals("unknown-user")) {
			Alert E1 = new Alert(AlertType.ERROR);
			E1.setHeaderText("User is unknown");
			E1.setContentText("Please try again");
			E1.show();
		}
	}
	
	public void timelinePane(ActionEvent event) {
		L1.setText("Successfully signed in");
		
		try {
			/* Load the timeline frame after login is successful */
			Stage stage = new Stage();
			FXMLLoader loader = new FXMLLoader(); // need a new instance to load a new frame
			Pane P1 = loader.load(getClass().getResource("Timeline.fxml").openStream());
			TimelineController tc = (TimelineController) loader.getController();
			tc.showTimeline(clientSocket, oos, ois);
			Scene scene = new Scene(P1);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	
	public void suggestedFollowersPane(ActionEvent event) {
		try {
			/* Load the suggested followers frame after login is successful */
			Stage stage2 = new Stage();
			FXMLLoader loader2 = new FXMLLoader(); // need a new instance to load a new frame
			Pane P2 = loader2.load(getClass().getResource("SuggestedFollowers.fxml").openStream());
			SuggestedFollowersController sfc = (SuggestedFollowersController) loader2.getController();
			sfc.showSuggested(clientSocket, oos, ois);
			Scene scene2 = new Scene(P2);
			scene2.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage2.setScene(scene2);
			stage2.show();
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
}
