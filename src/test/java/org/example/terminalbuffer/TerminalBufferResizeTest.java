package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferResizeTest {

    @Test
    void resizeWiderKeepsContentAndPads() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);
        b.write("abc");
        b.setCursor(0, 1);
        b.write("xy");

        b.resize(5, 2);

        assertEquals(5, b.width());
        assertEquals(2, b.height());
        assertEquals("abc  ", b.getLineAsString(b.scrollbackSize()));
        assertEquals("xy   ", b.getLineAsString(b.scrollbackSize() + 1));
    }

    @Test
    void resizeNarrowerTruncatesRightSide() {
        TerminalBuffer b = new TerminalBuffer(5, 1, 10);
        b.write("abcde");

        b.resize(3, 1);

        assertEquals(3, b.width());
        assertEquals(1, b.height());
        assertEquals("abc", b.getLineAsString(b.scrollbackSize()));
    }

    @Test
    void resizeShorterMovesTopLinesToScrollback() {
        TerminalBuffer b = new TerminalBuffer(3, 3, 10);
        b.setCursor(0, 0);
        b.write("111");
        b.setCursor(0, 1);
        b.write("222");
        b.setCursor(0, 2);
        b.write("333");

        b.resize(3, 2);

        assertEquals(1, b.scrollbackSize());
        assertEquals("111", b.getLineAsString(0));
        assertEquals("222", b.getLineAsString(1));
        assertEquals("333", b.getLineAsString(2));
    }

    @Test
    void resizeTallerAddsEmptyLines() {
        TerminalBuffer b = new TerminalBuffer(3, 1, 10);
        b.write("abc");

        b.resize(3, 3);

        assertEquals("abc\n   \n   ", b.getScreenAsString());
    }

    @Test
    void resizeClampsCursor() {
        TerminalBuffer b = new TerminalBuffer(5, 5, 10);
        b.setCursor(4, 4);

        b.resize(3, 2);

        assertEquals(2, b.cursorCol());
        assertEquals(1, b.cursorRow());
    }
}
