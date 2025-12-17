## Connect 4 – JavaFX Client/Server Application

This project is a networked Connect 4 game written in Java. It uses JavaFX for the graphical user interface and follows a client–server model so that two players can connect and play against each other over the network.

The client and server are separate applications. The server handles player connections, game logic, and move validation. The client is responsible for displaying the game board, handling user input, and communicating moves to the server.

## Project Structure

The repository contains two independent Maven projects:

connect4-game/

* client/   JavaFX client application
* server/   Game server application

Each folder has its own pom.xml and source files. Both projects can be built and run independently.

## Technologies Used

* Java
* JavaFX
* Maven
* Socket-based networking
* Git and GitHub

## How to Build

Navigate into the appropriate folder and run Maven.

Server:
cd server
mvn clean package

Client:
cd client
mvn clean package

## How to Run

Start the server first, then run the client. Two client instances are required to play a full game.

Server:
cd server
mvn javafx:run

Client:
cd client
mvn javafx:run

## Notes

* Build output folders (such as target/) and IDE-specific files are ignored using a .gitignore file.
* The client and server must be run as separate processes.
