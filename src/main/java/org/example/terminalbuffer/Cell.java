package org.example.terminalbuffer;

public final class Cell {
    public static final int EMPTY = 0;
    public static final int CONTINUATION = -1;

    private int codePoint;
    private TextAttributes attrs;

    public Cell(int codePoint, TextAttributes attrs) {
        this.codePoint = codePoint;
        this.attrs = attrs;
    }

    public static Cell empty() {
        return new Cell(EMPTY, TextAttributes.defaults());
    }

    public int codePoint() {
        return codePoint;
    }

    public TextAttributes attrs() {
        return attrs;
    }

    public void set(int codePoint, TextAttributes attrs) {
        this.codePoint = codePoint;
        this.attrs = attrs;
    }

    public Cell copy() {
        return new Cell(codePoint, attrs);
    }
}
