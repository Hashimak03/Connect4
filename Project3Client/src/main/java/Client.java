import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;




public class Client extends Thread{


	Socket socketClient;

	ObjectOutputStream out;
	ObjectInputStream in;
	public volatile boolean expectingPlayAgainResponse = false;
	public boolean enteringUsername = false;
	Board clientBoard;
	String currentText;
	String username;
	private final GuiClient gui;

    public Client(GuiClient gui) {
        this.gui = gui;
    }


    public void run() {

		try {
			socketClient= new Socket("127.0.0.1",5555);
	    	out = new ObjectOutputStream(socketClient.getOutputStream());
	    	in = new ObjectInputStream(socketClient.getInputStream());
	   	 	socketClient.setTcpNoDelay(true);

		}
		catch(Exception e) {}

		while(true) {

			try {
				Message message = (Message) in.readObject();

				if (message.type == MessageType.CHAT) {
					currentText = message.getString();
					System.out.println(message.getString());
					Platform.runLater(() -> gui.updateChatBox(message.getString()));
				}

				else if (message.type == MessageType.MOVE) {
					System.out.println(message.getMove());
					if (clientBoard != null) {
						Platform.runLater(() -> gui.updateBoardUI(clientBoard));
					}
				}

				else if (message.type == MessageType.BOARD) {
					message.getBoard().displayBoard();
					clientBoard = message.getBoard();
					Platform.runLater(() -> gui.updateBoardUI(message.getBoard()));
				}

				else if (message.type == MessageType.GAMEOVER) {
					currentText = message.getString();
					System.out.println(message.getString());
					Platform.runLater(() -> {
						gui.updateResultText(message.getString());
						gui.primaryStage.setScene(gui.resultScene);
					});
				}

				else if (message.type == MessageType.PLAY_AGAIN_REQUEST) {
					currentText = message.getString();
					System.out.println(message.getString());
					expectingPlayAgainResponse = true;
					Platform.runLater(() -> {
						gui.updateResultText(message.getString());
						gui.primaryStage.setScene(gui.resultScene);
					});
				}

				else if (message.type == MessageType.USERNAME_REQUEST) {
					System.out.println(message.getString());
					enteringUsername = true;
				}

				else if (message.type == MessageType.USERNAME_ACCEPTED) {
					System.out.println(message.getString());

					username = message.getString();
					enteringUsername = false;
				}

				else if (message.type == MessageType.START_GAME) {
					Platform.runLater(() -> gui.primaryStage.setScene(gui.gameScene));
				}

				/*else if (message.type == MessageType.REMATCH_DENIED) {
					Platform.runLater(() -> gui.primaryStage.setScene(gui.opponentLeftScene));
				}*/


				else if (message.type == MessageType.DISCONNECT) {
					Platform.runLater(() -> gui.primaryStage.setScene(gui.entryScene));
					sendDisconnect();
					disconnect();
					break;

				}
			}
			catch(Exception e) {}
		}

    }

	public void sendChat(String text) {

		try {
			out.writeObject(new Message(text, MessageType.CHAT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendMove(int move) {

		try {
			out.writeObject(new Message(move));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendDisconnect() {
		try {
			out.writeObject(new Message(MessageType.DISCONNECT));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void sendPlayAgain(boolean playAgain) {
		try {
			out.writeObject(new Message(playAgain));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendUsername(String username) {
		try {
			out.writeObject(new Message(username, MessageType.USERNAME_RESPONSE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() throws IOException {
		if (in != null) in.close();
		if (out != null) out.close();
		if (socketClient != null && !socketClient.isClosed()) {
			try {
				socketClient.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}


}
