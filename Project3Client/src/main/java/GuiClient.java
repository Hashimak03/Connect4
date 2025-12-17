import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;

public class GuiClient extends Application{

	public Stage primaryStage;
	public Scene entryScene;
	private Scene loginScene;
	public Scene gameScene;
	public Scene resultScene;
	private Scene waitingScene;
	public Scene opponentLeftScene;
	private Client clientThread;


	Circle[][] circleGrid = new Circle[6][7];

	public void updateBoardUI(Board board) {
		for (int r = 0; r < 6; r++) {
			for (int c = 0; c < 7; c++) {
				Cell cell = board.getCell(r, c);
				Color fill = (cell == Cell.RED)    ? Color.RED
						: (cell == Cell.YELLOW) ? Color.YELLOW
						: Color.BLACK;
				circleGrid[r][c].setFill(fill);
			}
		}
	}


	private String cut(String s) {
		if (s.length() <= 40) return s;
		return s.substring(0, 40);
	}

	List<Label> messageLabels;
	public void updateChatBox(String text) {
		if (text == null || text.isEmpty()) return;

		// shift everything up, cutting as we go
		for (int i = 0; i < messageLabels.size() - 1; i++) {
			String next = messageLabels.get(i + 1).getText();
			messageLabels.get(i).setText(cut(next));
		}

		// put the new message (cut down) into the bottom slot
		messageLabels.get(messageLabels.size() - 1)
				.setText(cut(text));
	}

	public void updateResultText(String result) {
		// Find the result text in the result scene
		for (javafx.scene.Node node : ((Pane)resultScene.getRoot()).getChildren()) {
			if (node instanceof Text && ((Text)node).getText().startsWith("You")) {
				((Text)node).setText(result);
				break;
			}
		}
	}

	private TextField usernameField;
	private void resetLoginScene() {
		usernameField.setDisable(false);
		usernameField.clear();
		usernameField.setPromptText("Enter Username (<10)");
	}



	public static void main(String[] args) {

		launch(args);


	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;

		createEntryScene(); // Main Menu
		createLoginScene(); // LogIn
		createGameScene(); // Actual Board
		createResultScene(); // Result, win, loss or draw
		createWaitingScene(); // Waiting (if user chooses rematch)
		createOpponentLeftScene(); // Opponent left when user clicks rematch but opponents clicks main menu

		primaryStage.setScene(entryScene); // Sets first scene to the Main Menu
		primaryStage.setTitle("Connect 4");
		primaryStage.show();
	}

	private void createEntryScene() {
		Pane entryRoot = new Pane();
		entryRoot.setStyle("-fx-background-color: #205D7B;"); // Background color, darker blueish grey color

		Text connectText = new Text("CONNECT"); // title
		connectText.setFont(Font.font("Impact", 126)); // title font
		connectText.setFill(Color.NAVY);
		connectText.setLayoutX(180);
		connectText.setLayoutY(300);

		Text numberText = new Text("4");
		numberText.setFont(Font.font("Impact", 222));
		numberText.setFill(Color.DARKRED);
		numberText.setLayoutX(625);
		numberText.setLayoutY(340);

		Button playButton = new Button("Play!");
		playButton.setStyle("-fx-background-color: NAVY; -fx-text-fill: white; -fx-font-size: 32px; -fx-font-family: 'Impact'; -fx-background-radius: 20; -fx-padding: 10 30 10 30;");
		playButton.setFont(Font.font("Impact", 32));
		playButton.setLayoutX(300);
		playButton.setLayoutY(470);
		playButton.setMinWidth(300);
		playButton.setPrefWidth(300);
		playButton.setOnAction(e ->  {

			if (clientThread != null) {
				clientThread = null;
			}

			if (clientThread == null || !clientThread.isAlive()) {
				clientThread = new Client(this);
				clientThread.start();
			}
			resetLoginScene();
			primaryStage.setScene(loginScene);
		}); // When play is clicked go to LogIn screen

		entryRoot.getChildren().addAll(connectText, numberText, playButton);
		entryScene = new Scene(entryRoot, 900, 800);
	}

	private void createLoginScene() {
		Pane loginRoot = new Pane();
		loginRoot.setStyle("-fx-background-color: #205D7B;");

		Text connectText = new Text("CONNECT");
		connectText.setFont(Font.font("Impact", 126));
		connectText.setFill(Color.NAVY);
		connectText.setLayoutX(180);
		connectText.setLayoutY(300);

		Text numberText = new Text("4");
		numberText.setFont(Font.font("Impact", 222));
		numberText.setFill(Color.DARKRED);
		numberText.setLayoutX(625);
		numberText.setLayoutY(340);

		// Ignore, for design purposes
		Rectangle box = new Rectangle();
		box.setWidth(400);
		box.setHeight(230);
		box.setFill(Color.DARKBLUE);
		box.setLayoutX(250);
		box.setLayoutY(340);
		box.setArcWidth(20);
		box.setArcHeight(20);

		Text promptText = new Text("Enter Username");
		promptText.setFont(Font.font("Impact", 32));
		promptText.setFill(Color.WHITE);
		promptText.setLayoutX(345);
		promptText.setLayoutY(400);

		// Username textfield FIXME
		usernameField = new TextField();
		usernameField.setStyle("-fx-font-size: 24px;");
		usernameField.setPrefWidth(300);
		usernameField.setLayoutX(300);
		usernameField.setLayoutY(420);
		usernameField.setPromptText("Enter Username (<10)");

		usernameField.setOnAction(event -> { //FIXME
			String entered = usernameField.getText().trim();
			if (entered.isEmpty()) {
				usernameField.clear();
				usernameField.setPromptText("Username cannot be empty");
				return;
			}
			else if (entered.length() > 10) {
				usernameField.clear();
				usernameField.setPromptText("Too many characters");
				return;
			}

			// 1) send the username to the server
			clientThread.sendUsername(entered);

			// 2) disable the field & show waiting prompt
			usernameField.clear();
			usernameField.setDisable(true);
			usernameField.setPromptText("Waiting for server…");

			// BLOCK the FX thread for 1 second to give the network thread time to respond
			try {
				Thread.sleep(100);  // bump if server needs more time
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			if (!clientThread.enteringUsername) {
				usernameField.clear();
				//usernameField.setDisable(true);
				usernameField.setPromptText("Waiting for opponent…");
			}

			else {
				usernameField.clear();
				usernameField.setDisable(false);
				usernameField.setPromptText("Username taken");
			}
		});

		loginRoot.getChildren().addAll(connectText, numberText, box, promptText, usernameField);
		loginScene = new Scene(loginRoot, 900, 800);
	}


	private void createGameScene() {

		clientThread = new Client(this);
		clientThread.start();

		Pane root = new Pane();
		root.setStyle("-fx-background-color: #205D7B;");

		Text connectText = new Text("CONNECT");
		connectText.setFont(Font.font("Impact", 84));
		connectText.setFill(Color.NAVY);
		connectText.setLayoutX(273);
		connectText.setLayoutY(126);

		Text numberText = new Text("4");
		numberText.setFont(Font.font("Impact", 148));
		numberText.setFill(Color.DARKRED);
		numberText.setLayoutX(575);
		numberText.setLayoutY(154);

		/*Text youText = new Text("You:");
		youText.setFont(Font.font("Impact", 32));
		youText.setFill(Color.WHITE);
		youText.setLayoutX(80);
		youText.setLayoutY(246);*/


//        Image playerColor = null;
//        if (player.color == 'RED') { // FIXME
//            playerColor = new Image(getClass().getResource("redC4.png").toExternalForm());
//        } else {
//            playerColor = new Image(getClass().getResource("yellowC4.png").toExternalForm());
//        }
//        ImageView playerColorImage = new ImageView(playerColor);
//		  playerColorImage.setLayoutX(115); X-coordinate guesstimate, fix
//		  playerColorImage.setLayoutY(246);


		// FIXME set who's turn it is here with their username
		// Text turnText = new Text(player.getUsername() + "\nturn");
		/*Text turnText = new Text("x's\nturn");
		turnText.setFont(Font.font("Impact", 32));
		turnText.setFill(Color.WHITE);
		turnText.setTextAlignment(TextAlignment.CENTER);
		turnText.setLayoutX(80);
		turnText.setLayoutY(430);*/


		/*
		########## CHATBOX SECTION STARTS ##########
		 */

		VBox messageBox = new VBox(5);
		messageBox.setLayoutX(27);
		messageBox.setLayoutY(640);
		messageBox.setStyle("-fx-background-color: #0A2463; -fx-padding: 10; -fx-background-radius: 10;");

		// create 4 message labels (initialize empty)
		messageLabels = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			Label messageLabel = new Label();
			messageLabel.setFont(Font.font("Impact", 11));
			messageLabel.setTextFill(Color.WHITE);
			messageLabel.setPrefWidth(200);
			messageLabel.setWrapText(true);
			messageLabel.setStyle("-fx-background-color: transparent;");
			messageLabels.add(messageLabel);
			messageBox.getChildren().add(messageLabel);
		}

		TextField inputField = new TextField();

		inputField.setOnAction(event -> {
			String username = clientThread.username;
			String message = username + ": " + inputField.getText().trim();
			clientThread.sendChat(message);

			inputField.clear();

		});

		messageBox.getChildren().add(inputField);

		root.getChildren().add(messageBox);

		/*
		########## CHATBOX SECTION ENDS ##########
		 */


		Rectangle board = new Rectangle(); // board (just a blue square)
		board.setWidth(600);
		board.setHeight(500);
		board.setFill(Color.DARKBLUE);
		board.setLayoutX(275);
		board.setLayoutY(220);

		root.getChildren().add(board);


		/*
		########## BOARD SECTION STARTS ##########
		 */

		// just before the loops, re-init the grid:
		circleGrid = new Circle[6][7];

		double circleDiameter = 70;
		double padding        = 10;
		double startX         = board.getLayoutX() + (board.getWidth()  - (7 * (circleDiameter + padding))) / 2;
		double startY         = board.getLayoutY() + (board.getHeight() - (6 * (circleDiameter + padding))) / 2;

		// build the circles and stash each into circleGrid[row][col]
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 7; col++) {
				Circle circle = new Circle(circleDiameter / 2);
				double x = startX + col * (circleDiameter + padding) + circleDiameter / 2 + 5;
				double y = startY + row * (circleDiameter + padding) + circleDiameter / 2 + 2;
				circle.setCenterX(x);
				circle.setCenterY(y);
				circle.setFill(Color.BLACK);

				// store it for later updates:
				circleGrid[row][col] = circle;
				root.getChildren().add(circle);
			}
		}

		/*
		########## BOARD SECTION ENDS ##########
		 */



		/*
		########## BUTTONS SECTION STARTS ##########
		 */

		Pane columnButtons = new Pane();
		columnButtons.setLayoutY(730);

		// plots the buttons for user input
		for (int col = 0; col < 7; col++) {
			Circle circle = new Circle(30, Color.ORANGE);
			circle.setStroke(Color.BLACK);
			circle.setStrokeWidth(2);

			Text label = new Text("C" + (col + 1));
			label.setFont(Font.font("Impact", 20));
			label.setFill(Color.BLACK);

			double centerOfX = 335 + (col * 80); // inital x value plus ith spacing
			circle.setCenterX(centerOfX);
			circle.setCenterY(30);
			label.setX(centerOfX - 10);
			label.setY(35);

			StackPane button = new StackPane();
			button.getChildren().addAll(circle, label);
			button.setLayoutX(centerOfX - 30);
			button.setLayoutY(0);

			int column = col; // can't directly put col into lambda for some reason
			button.setOnMouseClicked(e -> { // makes a function for each button FIXME this is where you put the game logic i think
				System.out.println("Column " + (column + 1) + " selected");
				clientThread.sendMove(column);
			});

			columnButtons.getChildren().add(button);
		}

		root.getChildren().add(columnButtons);

		/*
		########## BUTTONS SECTION ENDS ##########
		 */

		root.getChildren().addAll(connectText, numberText);
		gameScene = new Scene(root, 900, 800);

	}

	private void createResultScene() { // You need to set the result logic here, result word can be either won, lost or drew
		Pane resultRoot = new Pane();
		resultRoot.setStyle("-fx-background-color: #205D7B;");

		Text connectText = new Text("CONNECT");
		connectText.setFont(Font.font("Impact", 126));
		connectText.setFill(Color.NAVY);
		connectText.setLayoutX(180);
		connectText.setLayoutY(180);

		Text numberText = new Text("4");
		numberText.setFont(Font.font("Impact", 222));
		numberText.setFill(Color.DARKRED);
		numberText.setLayoutX(625);
		numberText.setLayoutY(220);


		Rectangle box = new Rectangle();
		box.setWidth(400);
		box.setHeight(250);
		box.setFill(Color.DARKBLUE);
		box.setLayoutX(250);
		box.setLayoutY(300);
		box.setArcWidth(20);
		box.setArcHeight(20);

//		if (board.result == 'WIN') { FIXME
//			result =
//		}
		Text resultText = new Text("You x!"); // Set result word, either won, lost or drew
		resultText.setFont(Font.font("Impact", 32));
		resultText.setFill(Color.WHITE);
		resultText.setLayoutX(410);
		resultText.setLayoutY(360);

		Button rematchButton = new Button("Rematch");
		rematchButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 32px; -fx-font-family: 'Impact'; -fx-background-radius: 20; -fx-padding: 10 30 10 30;");

		rematchButton.setMinWidth(300);
		rematchButton.setPrefWidth(300);
		rematchButton.setLayoutX(305);
		rematchButton.setLayoutY(380);
		rematchButton.setOnAction(e -> {
			clientThread.sendPlayAgain(true);
			primaryStage.setScene(waitingScene);
		});

		Button menuButton = new Button("Main Menu");
		menuButton.setStyle(
				"-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 32px; -fx-font-family: 'Impact'; -fx-background-radius: 20; -fx-padding: 10 30 10 30;"
		);

		menuButton.setMinWidth(300);
		menuButton.setPrefWidth(300);
		menuButton.setLayoutX(305);
		menuButton.setLayoutY(450);
		menuButton.setOnAction(e -> {
			clientThread.sendPlayAgain(false);
			primaryStage.setScene(entryScene);
		});


		resultRoot.getChildren().addAll(connectText, numberText, box, resultText, rematchButton, menuButton);
		resultScene = new Scene(resultRoot, 900, 800);
	}

	private void createWaitingScene() { // rematch logic needs to be set here
		Pane waitingRoot = new Pane();
		waitingRoot.setStyle("-fx-background-color: #205D7B;");

		Text connectText = new Text("CONNECT");
		connectText.setFont(Font.font("Impact", 126));
		connectText.setFill(Color.NAVY);
		connectText.setLayoutX(180);
		connectText.setLayoutY(180);

		Text numberText = new Text("4");
		numberText.setFont(Font.font("Impact", 222));
		numberText.setFill(Color.DARKRED);
		numberText.setLayoutX(625);
		numberText.setLayoutY(220);

		Rectangle box = new Rectangle();
		box.setWidth(400);
		box.setHeight(250);
		box.setFill(Color.DARKBLUE);
		box.setLayoutX(250);
		box.setLayoutY(300);
		box.setArcWidth(20);
		box.setArcHeight(20);

		Text waitingText = new Text("Waiting for opponent...");
		waitingText.setFont(Font.font("Impact", 32));
		waitingText.setFill(Color.WHITE);
		waitingText.setLayoutX(315);
		waitingText.setLayoutY(380);

		Button menuButton = new Button("Main Menu");
		menuButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 32px; -fx-font-family: 'Impact'; -fx-background-radius: 20; -fx-padding: 10 30 10 30;");

		menuButton.setMinWidth(300);
		menuButton.setPrefWidth(300);
		menuButton.setLayoutX(305);
		menuButton.setLayoutY(450);

		menuButton.setOnAction(e -> {

			clientThread.sendDisconnect();
			try {
				clientThread.disconnect();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			primaryStage.setScene(entryScene);

		});


		waitingRoot.getChildren().addAll(connectText, numberText, box, waitingText, menuButton);
		waitingScene = new Scene(waitingRoot, 900, 800);
	}

	private void createOpponentLeftScene() { // Nothing needs to be changed here i think
		Pane opponentLeftRoot = new Pane();
		opponentLeftRoot.setStyle("-fx-background-color: #205D7B;");

		Text connectText = new Text("CONNECT");
		connectText.setFont(Font.font("Impact", 126));
		connectText.setFill(Color.NAVY);
		connectText.setLayoutX(180);
		connectText.setLayoutY(180);

		Text numberText = new Text("4");
		numberText.setFont(Font.font("Impact", 222));
		numberText.setFill(Color.DARKRED);
		numberText.setLayoutX(625);
		numberText.setLayoutY(220);

		Rectangle box = new Rectangle();
		box.setWidth(400);
		box.setHeight(250);
		box.setFill(Color.DARKBLUE);
		box.setLayoutX(250);
		box.setLayoutY(300);
		box.setArcWidth(20);
		box.setArcHeight(20);

		Text opponentLeftText = new Text("Opponent does not want \nto play again.");
		opponentLeftText.setFont(Font.font("Impact", 32));
		opponentLeftText.setFill(Color.WHITE);
		opponentLeftText.setTextAlignment(TextAlignment.CENTER);
		opponentLeftText.setLayoutX(295);
		opponentLeftText.setLayoutY(380);

		Button menuButton = new Button("Main Menu");
		menuButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-font-size: 32px; -fx-font-family: 'Impact'; -fx-background-radius: 20; -fx-padding: 10 30 10 30;");

		menuButton.setMinWidth(300);
		menuButton.setPrefWidth(300);
		menuButton.setLayoutX(305);
		menuButton.setLayoutY(450);

		menuButton.setOnAction(e -> {

			clientThread.sendDisconnect();
            try {
                clientThread.disconnect();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            primaryStage.setScene(entryScene);

		});


		opponentLeftRoot.getChildren().addAll(connectText, numberText, box, opponentLeftText, menuButton);
		opponentLeftScene = new Scene(opponentLeftRoot, 900, 800);
	}

}
		

	




