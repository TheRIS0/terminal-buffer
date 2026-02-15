package org.example.terminalbuffer;

public final class Line {
    private final Cell[] cells;

    public Line(int width) {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        this.cells = new Cell[width];
        for (int i = 0; i < width; i++) {
            cells[i] = Cell.empty();
        }
    }

    public int width() {
        return cells.length;
    }

    public Cell cell(int col) {
        return cells[col];
    }

    public void clear() {
        for (int i = 0; i < cells.length; i++) {
            cells[i].set(Cell.EMPTY, TextAttributes.defaults());
        }
    }

    public Line deepCopy() {
        Line copy = new Line(cells.length);
        for (int i = 0; i < cells.length; i++) {
            Cell c = cells[i];
            copy.cells[i].set(c.codePoint(), c.attrs());
        }
        return copy;
    }

    public String toPlainString() {
        StringBuilder sb = new StringBuilder(cells.length);
        for (Cell c : cells) {
            int cp = c.codePoint();
            sb.append(cp == Cell.EMPTY ? ' ' : (char) cp);
        }
        return sb.toString();
    }

    public Line resizedTo(int newWidth) {
        if (newWidth <= 0) throw new IllegalArgumentException("newWidth must be > 0");
        Line out = new Line(newWidth);
        int n = Math.min(this.cells.length, newWidth);
        for (int i = 0; i < n; i++) {
            Cell c = this.cells[i];
            out.cells[i].set(c.codePoint(), c.attrs());
        }
        return out;
    }
}
