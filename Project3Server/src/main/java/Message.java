import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 1L;

    public MessageType type;
    private String message;  //CHAT
    private int col;  //MOVE
    private Board board;
    public boolean playAgain;



    public Message(String input, MessageType type){
        this.type = type;
        message = input;
    }
    public Message(MessageType type){
        this.type = type;
    }


    public Message(boolean playAgain){
        type = MessageType.PLAY_AGAIN_RESPONSE;
        this.playAgain = playAgain;
    }

    public Message(int colNum) {
        type = MessageType.MOVE;
        this.col = colNum;
    }
    public Message(Board board) {
        type = MessageType.BOARD;
        this.board = board;
    }

    public String getString(){
        return message;
    }

    public int getMove() {
        return col;
    }

    public Board getBoard() {
        return board;
    }
    public boolean getPlayAgain() {
        return playAgain;
    }



}
