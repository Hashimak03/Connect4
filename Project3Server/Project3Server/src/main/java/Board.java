import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private int numRows = 6;
    private int numCols = 7;
    public Cell[][] board = new Cell[numRows][numCols];

    Board() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                board[i][j] = Cell.EMPTY;
            }
        }
    }

    Board(int r, int c) {
        numRows = r;
        numCols = c;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                board[i][j] = Cell.EMPTY;
            }
        }
    }

    public Cell getCell(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: " + r + ", " + c);
        }
        return board[r][c];
    }

    public void setCell(int r, int c, Cell cell) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            throw new IndexOutOfBoundsException("Invalid cell coordinates: " + r + ", " + c);
        }
        this.board[r][c] = cell;
    }

    int getNumRows() {
        return numRows;
    }
    int getNumCols() {
        return numCols;
    }

    public void reset() {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                this.board[i][j] = Cell.EMPTY;
            }
        }
    }

    public void displayBoard() {
        System.out.print("--------------");
        for (int i = 0; i < numRows; i++) {
            System.out.print("\n|");
            for (int j = 0; j < numCols; j++) {
                System.out.print(board[i][j].getColor() + "|");
            }
        }
        System.out.println("\n--------------");
    }
}
