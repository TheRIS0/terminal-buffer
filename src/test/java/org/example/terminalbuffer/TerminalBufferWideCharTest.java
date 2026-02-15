package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferWideCharTest {

    private static String emoji() {
        return new String(Character.toChars(0x1F600)); // 😀
    }

    @Test
    void writeEmojiConsumesTwoCells() {
        TerminalBuffer b = new TerminalBuffer(4, 1, 10);
        String e = emoji();

        b.write("A" + e + "B");

        String line = b.getLineAsString(0);
        int c0 = b.getCodePointAt(0, 0);
        int c1 = b.getCodePointAt(0, 1);
        int c2 = b.getCodePointAt(0, 2);
        int c3 = b.getCodePointAt(0, 3);

        assertEquals("A" + e + " B", line, "line=" + line + " cells=" + c0 + "," + c1 + "," + c2 + "," + c3);

        assertEquals('A', c0);
        assertEquals(0x1F600, c1);
        assertEquals(Cell.CONTINUATION, c2);
        assertEquals('B', c3);
    }

    @Test
    void writeWideAtLastColumnWrapsToNextLine() {
        TerminalBuffer b = new TerminalBuffer(4, 2, 10);
        String e = emoji();

        b.setCursor(3, 0);
        b.write(e);

        assertEquals("    ", b.getLineAsString(0));
        assertEquals(e + "   ", b.getLineAsString(1));

        assertEquals(0x1F600, b.getCodePointAt(1, 0));
        assertEquals(Cell.CONTINUATION, b.getCodePointAt(1, 1));
    }

    @Test
    void insertEmojiShiftsAndUsesTwoCells() {
        TerminalBuffer b = new TerminalBuffer(5, 1, 10);
        String e = emoji();

        b.write("ab");
        b.setCursor(1, 0);
        b.insert(e);

        String line = b.getLineAsString(0);
        int c0 = b.getCodePointAt(0, 0);
        int c1 = b.getCodePointAt(0, 1);
        int c2 = b.getCodePointAt(0, 2);
        int c3 = b.getCodePointAt(0, 3);
        int c4 = b.getCodePointAt(0, 4);

        assertEquals("a" + e + " b ", line, "line=" + line + " cells=" + c0 + "," + c1 + "," + c2 + "," + c3 + "," + c4);

        assertEquals('a', c0);
        assertEquals(0x1F600, c1);
        assertEquals(Cell.CONTINUATION, c2);
        assertEquals('b', c3);
        assertEquals(Cell.EMPTY, c4);
    }
}
