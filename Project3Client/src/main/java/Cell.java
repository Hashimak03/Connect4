public enum Cell {
    RED("r"),
    YELLOW("y"),
    EMPTY(" ");

    private final String color;

    Cell(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}
