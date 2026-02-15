package org.example.terminalbuffer;

import java.util.Objects;

public final class TerminalBuffer {
    private final int width;
    private final int height;
    private final int scrollbackMax;

    private final Line[] screen;

    private final Line[] scrollbackStore;
    private int scrollbackStart;
    private int scrollbackSize;

    private int cursorCol;
    private int cursorRow;

    private TextAttributes currentAttrs = TextAttributes.defaults();

    public TerminalBuffer(int width, int height, int scrollbackMaxLines) {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        if (height <= 0) throw new IllegalArgumentException("height must be > 0");
        if (scrollbackMaxLines < 0) throw new IllegalArgumentException("scrollbackMaxLines must be >= 0");

        this.width = width;
        this.height = height;
        this.scrollbackMax = scrollbackMaxLines;

        this.screen = new Line[height];
        for (int i = 0; i < height; i++) {
            screen[i] = new Line(width);
        }

        this.scrollbackStore = new Line[scrollbackMaxLines == 0 ? 1 : scrollbackMaxLines];
        this.scrollbackStart = 0;
        this.scrollbackSize = 0;

        this.cursorCol = 0;
        this.cursorRow = 0;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int scrollbackMax() {
        return scrollbackMax;
    }

    public int scrollbackSize() {
        return scrollbackSize;
    }

    public TextAttributes currentAttributes() {
        return currentAttrs;
    }

    public void setCurrentAttributes(TextAttributes attrs) {
        this.currentAttrs = Objects.requireNonNull(attrs);
    }

    public void setCurrentAttributes(byte fg, byte bg, boolean bold, boolean italic, boolean underline) {
        int style = 0;
        if (bold) style |= TextAttributes.BOLD;
        if (italic) style |= TextAttributes.ITALIC;
        if (underline) style |= TextAttributes.UNDERLINE;
        setCurrentAttributes(new TextAttributes(fg, bg, style));
    }

    public void resetAttributes() {
        this.currentAttrs = TextAttributes.defaults();
    }

    public int cursorCol() {
        return cursorCol;
    }

    public int cursorRow() {
        return cursorRow;
    }

    public void setCursor(int col, int row) {
        cursorCol = clamp(col, 0, width - 1);
        cursorRow = clamp(row, 0, height - 1);
    }

    public void moveCursorUp(int n) {
        requireNonNegative(n);
        cursorRow = clamp(cursorRow - n, 0, height - 1);
    }

    public void moveCursorDown(int n) {
        requireNonNegative(n);
        cursorRow = clamp(cursorRow + n, 0, height - 1);
    }

    public void moveCursorLeft(int n) {
        requireNonNegative(n);
        cursorCol = clamp(cursorCol - n, 0, width - 1);
    }

    public void moveCursorRight(int n) {
        requireNonNegative(n);
        cursorCol = clamp(cursorCol + n, 0, width - 1);
    }

    public int totalLines() {
        return scrollbackSize + height;
    }

    private void pushToScrollback(Line line) {
        if (scrollbackMax == 0) return;

        if (scrollbackSize < scrollbackMax) {
            int idx = (scrollbackStart + scrollbackSize) % scrollbackStore.length;
            scrollbackStore[idx] = line;
            scrollbackSize++;
        } else {
            scrollbackStore[scrollbackStart] = line;
            scrollbackStart = (scrollbackStart + 1) % scrollbackStore.length;
        }
    }

    private void scrollUpOneLine() {
        pushToScrollback(screen[0].deepCopy());
        for (int i = 0; i < height - 1; i++) {
            screen[i] = screen[i + 1];
        }
        screen[height - 1] = new Line(width);
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static void requireNonNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0, got: " + n);
    }

    public void write(String text) {
        if (text == null || text.isEmpty()) return;

        Line line = screen[cursorRow];
        int col = cursorCol;

        for (int i = 0; i < text.length(); i++) {
            if (col >= width) break;
            char ch = text.charAt(i);
            line.cell(col).set(ch, currentAttrs);
            col++;
        }

        cursorCol = clamp(col, 0, width - 1);
    }

    public void fillLine(int row, int codePointOrZero) {
        if (row < 0 || row >= height) throw new IllegalArgumentException("row out of bounds: " + row);
        Line line = screen[row];
        for (int col = 0; col < width; col++) {
            line.cell(col).set(codePointOrZero, currentAttrs);
        }
    }

    public int getCodePointAt(int globalRow, int col) {
        checkGlobalRow(globalRow);
        checkCol(col);
        Line line = getLineByGlobalRow(globalRow);
        return line.cell(col).codePoint();
    }

    public TextAttributes getAttributesAt(int globalRow, int col) {
        checkGlobalRow(globalRow);
        checkCol(col);
        Line line = getLineByGlobalRow(globalRow);
        return line.cell(col).attrs();
    }

    public String getLineAsString(int globalRow) {
        checkGlobalRow(globalRow);
        Line line = getLineByGlobalRow(globalRow);
        return line.toPlainString();
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            sb.append(screen[i].toPlainString());
            if (i != height - 1) sb.append('\n');
        }
        return sb.toString();
    }

    public String getAllAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scrollbackSize; i++) {
            sb.append(getLineByGlobalRow(i).toPlainString()).append('\n');
        }
        for (int i = 0; i < height; i++) {
            sb.append(screen[i].toPlainString());
            if (i != height - 1) sb.append('\n');
        }
        return sb.toString();
    }

    private Line getLineByGlobalRow(int globalRow) {
        if (globalRow < scrollbackSize) {
            int idx = (scrollbackStart + globalRow) % scrollbackStore.length;
            return scrollbackStore[idx];
        }
        int screenRow = globalRow - scrollbackSize;
        return screen[screenRow];
    }

    private void checkGlobalRow(int globalRow) {
        if (globalRow < 0 || globalRow >= totalLines()) {
            throw new IllegalArgumentException("globalRow out of bounds: " + globalRow);
        }
    }

    private void checkCol(int col) {
        if (col < 0 || col >= width) {
            throw new IllegalArgumentException("col out of bounds: " + col);
        }
    }

    public void insertEmptyLineAtBottom() {
        scrollUpOneLine();
    }

    public void clearScreen() {
        for (int i = 0; i < height; i++) {
            screen[i] = new Line(width);
        }
        setCursor(0, 0);
    }

    public void clearAll() {
        clearScreen();
        scrollbackStart = 0;
        scrollbackSize = 0;
        for (int i = 0; i < scrollbackStore.length; i++) {
            scrollbackStore[i] = null;
        }
    }
}
