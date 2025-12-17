public class Connect4Game {

    private Board gameBoard = new Board();
    private Player redPlayer = null;
    private Player yellowPlayer = null;

    private Player currTurn;

    public void setCurrTurn(Player currPlayer) {
        this.currTurn = currPlayer;
    }
    public Player getCurrTurn() {
        return this.currTurn;
    }

    public Board getGameBoard() {
        return this.gameBoard;
    }

    public Player getRedPlayer() {
        return this.redPlayer;
    }
    public Player getYellowPlayer() {
        return this.yellowPlayer;
    }
    public void setRedPlayer(Player redPlayer) {
        this.redPlayer = redPlayer;
    }
    public void setYellowPlayer(Player yellowPlayer) {
        this.yellowPlayer = yellowPlayer;
    }

    public void display() {
        gameBoard.displayBoard();
    }

    //Updates gameBoard based on move made by player
    //Returns row number of successful move
    //Returns -1 if move is invalid
    //ARGS:
    //  Player currPlayer : Player Object of player who made the move
    //  int c : Column number that the player chose for their move
    public synchronized int makeMove(Player currPlayer, int c) {

        if (c < 0 || c >= gameBoard.getNumCols()) {
            return -1;
        }
        if (currPlayer != currTurn) {
            return -1;
        }

        for (int r = gameBoard.getNumRows() - 1; r >= 0; r--) {
            if (gameBoard.getCell(r, c) == Cell.EMPTY) {
                gameBoard.setCell(r, c, currPlayer.getPlayerCell());

                if (currTurn == Player.RED) {
                    currTurn = Player.YELLOW;
                }
                else {
                    currTurn = Player.RED;
                }
                return r;
            }
        }
        return -1;
    }


    //Checks whether the latest successful move results in a win
    //Returns true if the move is a win, return false otherwise
    //ARGS:
    //  int r : row number of last move
    //  int c : col number of last move
    //  Player currPlayer : player who made the last move
    public synchronized boolean checkWin(int r, int c, Player currPlayer) {


        //VERTICAL CHECKS
        int checkCount = 1;

        // Check down
        for (int i = 1; i <= 3; i++) {
            if (r + i < gameBoard.getNumRows() && gameBoard.getCell(r + i, c) == currPlayer.getPlayerCell()) {
                checkCount++;
            } else {
                break;
            }
        }

        // Check up
        for (int i = 1; i <= 3; i++) {
            if (r - i >= 0 && gameBoard.getCell(r - i, c) == currPlayer.getPlayerCell()) {
                checkCount++;
            } else {
                break;
            }
        }

        if (checkCount >= 4)  { return true; }


        //HORIZONTAL CHECKS
        checkCount = 1;

        // Check right
        for (int i = 1; i <= 3; i++) {
            if (c + i < gameBoard.getNumCols() && gameBoard.getCell(r, c + i) == currPlayer.getPlayerCell()) {
                checkCount++;
            } else {
                break;
            }
        }

        // Check left
        for (int i = 1; i <= 3; i++) {
            if (c - i >= 0 && gameBoard.getCell(r, c - i) == currPlayer.getPlayerCell()) {
                checkCount++;
            } else {
                break;
            }
        }

        if (checkCount >= 4) { return true; }


        //DIAGONAL CHECKS
        checkCount = 1;

        // Check up and right
        for (int i = 1; i <= 3; i++) {
            if (c + i < gameBoard.getNumCols() && r - i >= 0 &&
                gameBoard.getCell(r - i, c + i) == currPlayer.getPlayerCell()) {

                checkCount++;

            } else {
                break;
            }
        }

        // Check down and left
        for (int i = 1; i <= 3; i++) {
            if (c - i >= 0 && r + i < gameBoard.getNumRows()
                && gameBoard.getCell(r + i, c - i) == currPlayer.getPlayerCell()) {

                checkCount++;

            } else {
                break;
            }
        }

        if (checkCount >= 4) { return true; }


        checkCount = 1;

        // Check up and left
        for (int i = 1; i <= 3; i++) {
            if (c - i >= 0 && r - i >= 0 &&
                gameBoard.getCell(r - i, c - i) == currPlayer.getPlayerCell()) {

                checkCount++;

            } else {
                break;
            }
        }

        // Check down and right
        for (int i = 1; i <= 3; i++) {
            if (c + i < gameBoard.getNumCols() && r + i < gameBoard.getNumRows() &&
                gameBoard.getCell(r + i, c + i) == currPlayer.getPlayerCell()) {

                checkCount++;

            } else {
                break;
            }
        }

        if (checkCount >= 4) { return true; }

        return false;
    }

    //Checks whether the game is a draw
    //Returns true if drawn, otherwise false
    public boolean checkDraw() {

        for (int c = 0; c < gameBoard.getNumCols(); c++) {
            if (gameBoard.getCell(0, c) == Cell.EMPTY) {
                return false;
            }
        }

        return true;
    }

    public void resetBoard() {
        gameBoard.reset();
    }
}
