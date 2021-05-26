package com.bham.fsd.assignments.jabberserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.bham.fsd.assignments.jabberserver.TimelineController.Heart;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class SuggestedFollowersController implements Initializable {
	
	@FXML private Label WhoToFollow;
	@FXML private ListView<Follow> list;
	ObservableList<String> ol;
	Socket clientSocket;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	ArrayList<String> userArray = new ArrayList<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resource) {
		
	}

	public void showSuggested(Socket clientSocket, ObjectOutputStream oos, ObjectInputStream ois) {
		this.clientSocket = clientSocket;
		this.oos = oos;
		this.ois = ois;
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						try {
							JabberMessage jm = new JabberMessage("users");
							oos.writeObject(jm);
							oos.flush();
							
							JabberMessage response = (JabberMessage) ois.readObject();
							String message = response.getMessage();
							ArrayList<ArrayList<String>> data = response.getData();
							
							/*
							System.out.println(message);
							for(int i = 0; i < data.size(); i++) {
								System.out.println(data.get(i));
							}
							*/
							
							if(data != null && message.equals("users")) {
								List<Follow> users = new ArrayList<>();
								for(int i = 0; i < data.size(); i++) {
									String user = data.get(i).get(0);
									users.add(new Follow(user));
								}
								ObservableList<Follow> myObservableList = FXCollections.observableList(users);
						        list.setItems(myObservableList);
							}
						}
						catch(IOException | ClassNotFoundException e) {
							System.out.println(e);
						}
					}
				});
			}
		}, 0, 20000);
	}
	
	public class Follow extends HBox {
		Label label = new Label();
		Button btn = new Button();
		
		Follow(String username) {
			super();
			label.setText(username);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);

            Image image = new Image("green-plus-sign.png");
            ImageView view = new ImageView(image);
            view.setFitHeight(10);
            view.setPreserveRatio(true);
            btn.setGraphic(view);
            
            EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
            	public void handle(ActionEvent e) {
            		try {
            			if(!userArray.contains(username)) {
            				userArray.add(username);
	            			btn.setText("Following");
	            			JabberMessage jm = new JabberMessage("follow " + username);
	            			oos.writeObject(jm);
	            			oos.flush();
	            			JabberMessage message = (JabberMessage) ois.readObject();
	            			String m1 = message.getMessage(); // "Posted"- follow success
	            			
	            			JabberMessage followupMessage = (JabberMessage) ois.readObject();
	            			String m2 = followupMessage.getMessage(); // "Timeline"
	        				System.out.println(m2);
            			}
            		}
            		catch(IOException | ClassNotFoundException exception) {
            			System.out.println(exception);
            		}
            	}
            };
            btn.setOnAction(event);
            
            this.getChildren().addAll(label, btn);
		}
	}
}