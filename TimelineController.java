package com.bham.fsd.assignments.jabberserver;

import java.io.IOException;
import javafx.scene.image.*; 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class TimelineController implements Initializable {
	@FXML private Label Timeline;
	@FXML private ListView<Heart> list;
	ObservableList<String> ol;
	Socket clientSocket;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	@FXML private TextField T2;
	@FXML private Button B6;
	ArrayList<String> likesArray = new ArrayList<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resource) {
		
	}
	
	public void showTimeline(Socket clientSocket, ObjectOutputStream oos, ObjectInputStream ois) {
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
							JabberMessage jm = new JabberMessage("timeline");
							oos.writeObject(jm);
							oos.flush();
							
							/* Receives "timeline" message and ArrayList data from server */
							JabberMessage response = (JabberMessage) ois.readObject();
							String message = response.getMessage();
							ArrayList<ArrayList<String>> data = response.getData();
							
							if(data != null && message.equals("timeline")) {
								List<Heart> hList = new ArrayList<>();
								StringBuilder jab = new StringBuilder();
								for(int i = 0; i < data.size(); i++) {
									jab.append(data.get(i).get(0) + ": ");
									jab.append(data.get(i).get(1));
									hList.add(new Heart(String.valueOf(jab), data.get(i).get(3), data.get(i).get(2)));
									jab = new StringBuilder();
								}
						        ObservableList<Heart> myObservableList = FXCollections.observableList(hList);
						        list.setItems(myObservableList);
							}
						}
						catch(IOException | ClassNotFoundException e) {
							System.out.println(e);
						}
					}
				});
			}
		}, 0, 2000);
	}
	
	public void postJab(ActionEvent e) {
		try {
			JabberMessage jm = new JabberMessage("post " + T2.getText());
			System.out.println(jm.getMessage());
			oos.writeObject(jm);
			oos.flush();
			T2.clear();
		}
		catch(IOException  exception) {
			System.out.println(exception);
		}
	}
	
	ArrayList<Integer> jabLikes = new ArrayList<>();
	Boolean pressed = false;
	
	public class Heart extends HBox {
		Label label = new Label();
		Button btn = new Button();
		int count;
		
		Heart(String labelText, String likes, String jabID) {
            super();
            this.count = Integer.parseInt(likes);
            
            if(pressed == true) {
            	btn.setText(String.valueOf(count)); 
            }
            else {
            	btn.setText(likes);
            }

            label.setText(labelText);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
 
            Image image = new Image("heart.png");
            ImageView view = new ImageView(image);
            view.setFitHeight(10);
            view.setPreserveRatio(true);
            btn.setGraphic(view);
            
            EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
            	public void handle(ActionEvent e) {
            		try {
            			if(!likesArray.contains(jabID)) {
            				likesArray.add(jabID);
	            			count++;
	            			pressed = true;
	            			btn.setText(String.valueOf(count));
	            			//btn.setText(String.valueOf(count));
	            			JabberMessage jm = new JabberMessage("like " + jabID);
	            			oos.writeObject(jm);
	            			oos.flush();
	            			
	            			try {
	            				/* Forces to wait for response before refreshing the timeline, as otherwise the
	            				 * likes count would be re-rerendered with old data */
	            				JabberMessage response;
		            			while(!(response = (JabberMessage) ois.readObject()).getMessage().equals("posted")) {
		            				String message = response.getMessage();
									System.out.println(message);
		            			}
								String message = response.getMessage();
								System.out.println(message);
	            			}
	            			catch(IOException | ClassNotFoundException exception) {
	            				System.out.println(exception);
	            			}
            			}
            		}
            		catch(IOException exception) {
            			System.out.println(exception);
            		}
            	}
            };
            btn.setOnAction(event);

            this.getChildren().addAll(label, btn);
		}
	}
}