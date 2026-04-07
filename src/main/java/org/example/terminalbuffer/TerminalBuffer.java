package org.example.terminalbuffer;

import java.util.Objects;

public final class TerminalBuffer {
    private int width;
    private int height;
    private final int scrollbackMax;

    private Line[] screen;
    private int screenTopIndex;

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
        this.screenTopIndex = 0;

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
        this.currentAttrs = TextAttributes.intern(Objects.requireNonNull(attrs));
    }

    public void setCurrentAttributes(byte fg, byte bg, boolean bold, boolean italic, boolean underline) {
        int style = 0;
        if (bold) style |= TextAttributes.BOLD;
        if (italic) style |= TextAttributes.ITALIC;
        if (underline) style |= TextAttributes.UNDERLINE;
        this.currentAttrs = TextAttributes.intern(fg, bg, style);
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

    private int physicalScreenIndex(int logicalRow) {
        return (screenTopIndex + logicalRow) % height;
    }

    private Line screenLine(int logicalRow) {
        return screen[physicalScreenIndex(logicalRow)];
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
        pushToScrollback(screenLine(0).deepCopy());
        screenTopIndex = (screenTopIndex + 1) % height;
        screenLine(height - 1).clear();
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static void requireNonNegative(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0, got: " + n);
    }

    private static int cellWidth(int codePoint) {
        // Simple heuristic: treat emoji and CJK as width 2.
        // This is not perfect, but acceptable for the bonus.
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
            return 2;
        }

        // Emoji ranges (rough)
        if ((codePoint >= 0x1F300 && codePoint <= 0x1FAFF) || (codePoint >= 0x2600 && codePoint <= 0x26FF)) {
            return 2;
        }

        return 1;
    }

    public void write(String text) {
        if (text == null || text.isEmpty()) return;

        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            int w = cellWidth(cp);

            if (w == 1) {
                if (cursorCol >= width) break;
                Line line = screenLine(cursorRow);
                line.setCell(cursorCol, cp, currentAttrs);
                cursorCol = clamp(cursorCol + 1, 0, width - 1);
            } else {
                // need 2 cells
                if (cursorCol == width - 1) {
                    // no space: move to next line start (like wrap)
                    cursorCol = 0;
                    cursorRow++;
                    if (cursorRow >= height) {
                        scrollUpOneLine();
                        cursorRow = height - 1;
                    }
                }
                if (cursorCol >= width - 1) break;

                Line line = screenLine(cursorRow);
                line.setCell(cursorCol, cp, currentAttrs);
                line.setCell(cursorCol + 1, Cell.CONTINUATION, currentAttrs);

                cursorCol += 2;
                if (cursorCol >= width) {
                    cursorCol = 0;
                    cursorRow++;
                    if (cursorRow >= height) {
                        scrollUpOneLine();
                        cursorRow = height - 1;
                    }
                }
            }
        }
    }

    public void insert(String text) {
        if (text == null || text.isEmpty()) return;

        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            int w = cellWidth(cp);
            if (w == 1) {
                insertOneCell(cp, currentAttrs);
            } else {
                insertWide(cp, currentAttrs);
            }
        }
    }

    private void insertOneCell(int codePoint, TextAttributes attrs) {
        int carryCp = codePoint;
        TextAttributes carryAttrs = attrs;

        while (true) {
            Line line = screenLine(cursorRow);

            int overflowCp = line.codePointAt(width - 1);
            TextAttributes overflowAttrs = line.attrsAt(width - 1);

            for (int col = width - 1; col > cursorCol; col--) {
                line.setCell(col, line.codePointAt(col - 1), line.attrsAt(col - 1));
            }

            line.setCell(cursorCol, carryCp, carryAttrs);

            cursorCol++;

            if (cursorCol >= width) {
                cursorCol = 0;
                cursorRow++;

                if (cursorRow >= height) {
                    scrollUpOneLine();
                    cursorRow = height - 1;
                }
            }

            if (overflowCp == Cell.EMPTY) {
                return;
            }

            carryCp = overflowCp;
            carryAttrs = overflowAttrs;
        }
    }

    private void insertWide(int codePoint, TextAttributes attrs) {
        // If only one cell remains on this line, move to next line start
        if (cursorCol == width - 1) {
            cursorCol = 0;
            cursorRow++;
            if (cursorRow >= height) {
                scrollUpOneLine();
                cursorRow = height - 1;
            }
        }
        if (cursorCol >= width - 1) return;

        insertOneCell(codePoint, attrs);
        insertOneCell(Cell.CONTINUATION, attrs);
    }

    public void fillLine(int row, int codePointOrZero) {
        if (row < 0 || row >= height) throw new IllegalArgumentException("row out of bounds: " + row);
        Line line = screenLine(row);
        for (int col = 0; col < width; col++) {
            line.setCell(col, codePointOrZero, currentAttrs);
        }
    }

    public int getCodePointAt(int globalRow, int col) {
        checkGlobalRow(globalRow);
        checkCol(col);
        Line line = getLineByGlobalRow(globalRow);
        return line.codePointAt(col);
    }

    public TextAttributes getAttributesAt(int globalRow, int col) {
        checkGlobalRow(globalRow);
        checkCol(col);
        Line line = getLineByGlobalRow(globalRow);
        return line.attrsAt(col);
    }

    public String getLineAsString(int globalRow) {
        checkGlobalRow(globalRow);
        Line line = getLineByGlobalRow(globalRow);
        return line.toPlainString();
    }

    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < height; i++) {
            sb.append(screenLine(i).toPlainString());
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
            sb.append(screenLine(i).toPlainString());
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
        return screenLine(screenRow);
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

    public void resize(int newWidth, int newHeight) {
        if (newWidth <= 0) throw new IllegalArgumentException("newWidth must be > 0");
        if (newHeight <= 0) throw new IllegalArgumentException("newHeight must be > 0");

        if (newWidth == this.width && newHeight == this.height) return;

        if (newHeight < this.height) {
            int removed = this.height - newHeight;
            for (int i = 0; i < removed; i++) {
                pushToScrollback(screenLine(i).deepCopy());
            }
        }

        Line[] newScreen = new Line[newHeight];
        int oldLogicalOffset = (newHeight < this.height) ? (this.height - newHeight) : 0;
        int linesToCopy = Math.min(newHeight, this.height - oldLogicalOffset);
        for (int i = 0; i < linesToCopy; i++) {
            newScreen[i] = screenLine(oldLogicalOffset + i).resizedTo(newWidth);
        }
        for (int i = linesToCopy; i < newHeight; i++) {
            newScreen[i] = new Line(newWidth);
        }

        this.screen = newScreen;
        this.screenTopIndex = 0;
        this.width = newWidth;
        this.height = newHeight;

        setCursor(cursorCol, cursorRow);
    }

    public void insertEmptyLineAtBottom() {
        scrollUpOneLine();
    }

    public void clearScreen() {
        screenTopIndex = 0;
        for (int i = 0; i < height; i++) {
            screen[i].clear();
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
