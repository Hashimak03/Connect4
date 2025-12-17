//Helps the Message class define what type of message is being sent over the server

public enum MessageType {
    CHAT,
    MOVE,
    BOARD,
    GAMEOVER,
    PLAY_AGAIN_REQUEST,
    PLAY_AGAIN_RESPONSE,
    USERNAME_REQUEST,
    USERNAME_RESPONSE,
    USERNAME_ACCEPTED,
    START_GAME,
    REMATCH_DENIED,
    DISCONNECT;
}
