*** START ***

JabberServer is the class that needs executing first, as it contains the code for the server and creates a 
new ClientConnection thread for each client. Once the server is up and running, the View class needs to be
executed, which contains the code for the GUI and is controlled by the MainController class. Other 
facilitating controller classes have been created for each pane, and they get run when the relevant GUI
buttons are pressed.

The project refreshes the timeline of each client every 2 seconds to show in real-time how many likes the
jabs of users they follow have. It also updates to show users when someone they follow has posted a new jab.
The Who To Follow pane also gets updated every 20 seconds, suggesting to them new users who have just
registered and also removing from the suggested list those who they've just followed by pressing 
the follow button.

***** END *****