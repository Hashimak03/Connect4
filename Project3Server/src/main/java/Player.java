public enum Player {

    RED("red", Cell.RED),
    YELLOW("yellow", Cell.YELLOW),;

    private String username;
    private String color;
    private int numWins = 0;
    private int numLosses = 0;
    private Cell playerCell;

    Player(String color, Cell cell) {
        this.color = color;
        this.playerCell = cell;
    }

    public Cell getPlayerCell() {
        return playerCell;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public int getNumWins() {
        return numWins;
    }
    public void incNumWins() {
        numWins++;
    }

    public int getNumLosses() {
        return numLosses;
    }
    public void incNumLosses() {
        numLosses++;
    }

    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

}
