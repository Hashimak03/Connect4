import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;


public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	Queue<ClientThread> clientQueue = new LinkedList<>();
	TheServer server;
	
	
	Server(){

		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {
		
			try(ServerSocket mysocket = new ServerSocket(5555);){
		    System.out.println("Server is waiting for a client!");
		  
			
		    while(true) {

				ClientThread c = new ClientThread(mysocket.accept(), count);
				clients.add(c);
				c.start();

				count = clients.size() + 1;

				
			    }
			} catch(Exception e) {
					System.err.println("Server did not launch");
				}
			}
		}
	

		class ClientThread extends Thread{
			
		
			Socket connection;
			int threadCount;
			ObjectInputStream in;
			ObjectOutputStream out;
			Connect4Game game;
			Player thisPlayer;
			String username;
			ClientThread opponent;
			boolean playAgain = false;
			boolean opponentPlayAgain = false;
			boolean playAgainResponse = false;
			boolean validUsername = false;


			public void run(){
					
				try {
					out = new ObjectOutputStream(connection.getOutputStream());
					in = new ObjectInputStream(connection.getInputStream());
					connection.setTcpNoDelay(true);	
				}
				catch(Exception e) {
					System.out.println("Streams not open");
				}
				
				updateServer("new client on server: client #" + threadCount);


				//GETTING USERNAME FROM CLIENT
				sendMessage(new Message("Enter Username (<10): ", MessageType.USERNAME_REQUEST), this);

				while (!validUsername) {

                    Message message;
                    try {
                        message = (Message) in.readObject();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    if (message.type == MessageType.USERNAME_RESPONSE) {
						validUsername = checkValidUsername(message.getString(), this);
                    }

					if (validUsername) {
						username = message.getString().trim();
						System.out.println("Client #" + threadCount + "'s username is: " + username + "\n");
						sendMessage(new Message(username, MessageType.USERNAME_ACCEPTED), this);
					}
					else {
						sendMessage(new Message("Invalid Username!", MessageType.USERNAME_REQUEST), this);
					}

                }


                // Matchmaking logic
				synchronized (clientQueue) {
					if (clientQueue.isEmpty()) {
						clientQueue.add(this);
						thisPlayer = Player.RED;
						thisPlayer.setUsername(username);
                        try {
                            out.writeObject(new Message("Waiting for opponent...", MessageType.CHAT));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
						ClientThread opponentThread = clientQueue.poll();
						thisPlayer = Player.YELLOW;
						thisPlayer.setUsername(username);
						opponent = opponentThread;
						opponentThread.opponent = this;

						// Create a shared game object
						game = new Connect4Game();
						game.setCurrTurn(Player.RED);
						opponentThread.game = game;

						//Update server
						System.out.println("GAME STARTED: [" + opponent.thisPlayer.getUsername() + " vs " + thisPlayer.getUsername() + "]" + "\n");

						//Send game start message to both clients
                        sendMessage(new Message("You are YELLOW. Game started!", MessageType.CHAT), this);
                        sendMessage(new Message("Opponent found! You are RED. Your move.", MessageType.CHAT), opponent);

						//Send starting board to both clients
						resetOutStream(this);
						sendMessage(new Message(game.getGameBoard()), this);

						resetOutStream(opponent);
						sendMessage(new Message(game.getGameBoard()), opponent);

						//Switch clients over to game scene
						sendMessage(new Message(MessageType.START_GAME), this);
						sendMessage(new Message(MessageType.START_GAME), opponent);

					}
				}

				boolean running = true;
				while(running) {
					try {
						Message message = (Message) in.readObject();

						//If message is a chat
						if (message.type == MessageType.CHAT) {
							//System.out.println(message.getString());
							updateServer("client #" + threadCount + " [" + thisPlayer.getUsername() + "] said " + message.getString() + "\n");
							sendMessage(new Message(message.getString(), MessageType.CHAT), this);
							sendMessage(new Message(message.getString(), MessageType.CHAT), opponent);
						}

						//If message is a move
						else if (message.type == MessageType.MOVE) {


							int col = message.getMove();  //Get column for move
							int row = game.makeMove(thisPlayer, col);  //get row number for move, if move is valid

							//If move was made out of turn
							if (row == -1) {
								sendMessage(new Message("NOT YOUR TURN!", MessageType.CHAT), this);
							}

							//If valid move
							else {

								//Generate message for updated board
								Message boardUpdate = new Message(game.getGameBoard());

								//Send board update to this client
								resetOutStream(this);
								sendMessage(boardUpdate, this);

								//Send board update to opponent client
								resetOutStream(opponent);
								sendMessage(boardUpdate, opponent);

								//Check for a Win
								if (game.checkWin(row, col, thisPlayer)) {

									//Updating server
									System.out.println("GAME ENDED [" + thisPlayer.getUsername() + " vs " + opponent.thisPlayer.getUsername() + "]");
									System.out.println("Winner: " + thisPlayer.getUsername() + " | Loser: " + opponent.thisPlayer.getUsername() + "\n");

									//Send Win message to this client
									sendMessage(new Message("You Win!", MessageType.GAMEOVER), this);

									//Send Lose message to opponent client
									sendMessage(new Message("You Lose!", MessageType.GAMEOVER), opponent);

									//Ask client to play again
									sendMessage(new Message("You Win!", MessageType.PLAY_AGAIN_REQUEST), this);

									//Ask opponent to play again
									sendMessage(new Message("You Lose!", MessageType.PLAY_AGAIN_REQUEST), opponent);

								}

								//Check for Draw
								else if (game.checkDraw()) {

									//Update server
									System.out.println("GAME ENDED [" + thisPlayer.getUsername() + " vs " + opponent.thisPlayer.getUsername() + "]");
									System.out.println("The game was a DRAW!" + "\n");

									//Send Draw message to both clients
									sendMessage(new Message("It's a draw!", MessageType.GAMEOVER), this);
									sendMessage(new Message("It's a draw!", MessageType.GAMEOVER), opponent);

									//Ask client to play again
									sendMessage(new Message("It's a draw!", MessageType.PLAY_AGAIN_REQUEST), this);

									//Ask opponent to play again
									sendMessage(new Message("It's a draw!", MessageType.PLAY_AGAIN_REQUEST), opponent);
								}
							}

						}

						else if (message.type == MessageType.PLAY_AGAIN_RESPONSE) {
							synchronized (game) {
								this.playAgain = message.getPlayAgain();
								playAgainResponse = true;

								// Notify the opponent's thread (in case it is waiting)
								game.notifyAll();

								if (opponent.playAgainResponse) {
									//Both players want to play again
									if (this.playAgain && opponent.playAgain) {

										//Reset flags
										this.playAgain = false;
										opponent.playAgain = false;
										playAgainResponse = false;
										opponent.playAgainResponse = false;

										//Reset board
										game.resetBoard();
										game.setCurrTurn(Player.RED);

										//Update Server
										System.out.println("GAME STARTED: [" + thisPlayer.getUsername() + " vs " + opponent.thisPlayer.getUsername() + "]" + "\n");

										//Send rematch message to both clients
										if (thisPlayer == Player.RED) {
											sendMessage(new Message(MessageType.START_GAME), this);
											sendMessage(new Message(MessageType.START_GAME), opponent);
											sendMessage(new Message("Game Started! You are RED. Your move", MessageType.CHAT), this);
											sendMessage(new Message("Game Started! You are YELLOW", MessageType.CHAT), opponent);
										} else if (thisPlayer == Player.YELLOW) {
											sendMessage(new Message(MessageType.START_GAME), this);
											sendMessage(new Message(MessageType.START_GAME), opponent);
											sendMessage(new Message("Game Started! You are YELLOW", MessageType.CHAT), this);
											sendMessage(new Message("Game Started! You are RED. Your move", MessageType.CHAT), opponent);
										}

										//Send updated game board to both clients
										resetOutStream(this);
										sendMessage(new Message(game.getGameBoard()), this);

										resetOutStream(opponent);
										sendMessage(new Message(game.getGameBoard()), opponent);

									}
									else {

										//One or both clients do not wish to play, therefore disconnect

										//sendMessage(new Message("You do not want to play again", MessageType.CHAT), this);
										sendMessage(new Message(MessageType.DISCONNECT), this);

									}

								}
								else {
									// Wait for opponent's response
									try {
										game.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}

							}
						}

						else if (message.type == MessageType.DISCONNECT) {
							running = false;
							disconnect();
						}


					}
					catch(Exception e) {
						System.err.println("OOOOPPs...Something wrong with the socket from client: " + threadCount + "....closing down!");
						//updateClients(username + " has left the server!");
						clients.remove(this);


						break;
					}
				}
			}//end of run

			/*
			####### HELPER FUNCTIONS #######
			*/

			ClientThread(Socket s, int count){
				this.connection = s;
				threadCount = count;
			}

			public void updateClients(String text) {

				for (ClientThread c : clients) {
					try {
						if (c != null) {
							c.out.writeObject(new Message(text, MessageType.CHAT));
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

			}

			public void updateServer(String text) {
				System.out.println(text);
			}

			public void sendMessage(Message message, ClientThread client) {
				try {
					client.out.writeObject(message);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			public void resetOutStream(ClientThread client) {
				try {
					client.out.reset();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			public boolean checkValidUsername(String username, ClientThread client) {

				if (username == null) {
					return false;
				}
				username = username.trim();

				if (username.length() == 0 || username.length() > 10) {
					return false;
				}

				for (ClientThread c : clients) {
					if (c != client) {
						if ((c.thisPlayer != null && c.thisPlayer.getUsername() != null) &&
								(c.thisPlayer.getUsername().equals(username))) {
							return false;
						}
					}
				}
				return true;
			}

			public void disconnect() throws IOException {
				if (opponent != null) {
					//sendMessage(new Message("Opponent disconnected.", MessageType.CHAT), opponent);
					opponent.opponent = null;
				}
				System.out.println(thisPlayer.getUsername() + " has left the server!" + "\n");
				if (in != null) in.close();
				if (out != null) out.close();
				if (connection != null && !connection.isClosed()) {
					try {
						connection.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
			
			
		}//end of client thread
}


	
	

	
