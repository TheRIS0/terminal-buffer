package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferScrollbackAndClearTest {

    @Test
    void insertEmptyLineAtBottomScrollsTopToScrollback() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);
        b.write("abc");           // row 0
        b.setCursor(0, 1);
        b.write("xy");            // row 1: "xy "

        b.insertEmptyLineAtBottom();

        assertEquals(1, b.scrollbackSize());
        assertEquals("abc", b.getLineAsString(0));      // scrollback
        assertEquals("xy ", b.getLineAsString(1));      // screen row 0 after scroll
        assertEquals("   ", b.getLineAsString(2));      // new empty bottom row
    }

    @Test
    void scrollbackRespectsMaxSizeDropsOldest() {
        TerminalBuffer b = new TerminalBuffer(2, 2, 2);

        b.setCursor(0, 0);
        b.write("11");
        b.insertEmptyLineAtBottom(); // scrollback: "11"

        b.setCursor(0, 0);
        b.write("22");
        b.insertEmptyLineAtBottom(); // scrollback: "11","22"

        b.setCursor(0, 0);
        b.write("33");
        b.insertEmptyLineAtBottom(); // scrollback should keep last 2: "22","33"

        assertEquals(2, b.scrollbackSize());
        assertEquals("22", b.getLineAsString(0));
        assertEquals("33", b.getLineAsString(1));
    }

    @Test
    void clearScreenDoesNotClearScrollback() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);
        b.write("abc");
        b.insertEmptyLineAtBottom();
        assertEquals(1, b.scrollbackSize());

        b.clearScreen();

        assertEquals(1, b.scrollbackSize());
        assertEquals("abc", b.getLineAsString(0));
        assertEquals("   \n   ", b.getScreenAsString());
    }

    @Test
    void clearAllClearsBoth() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);
        b.write("abc");
        b.insertEmptyLineAtBottom();
        assertEquals(1, b.scrollbackSize());

        b.clearAll();

        assertEquals(0, b.scrollbackSize());
        assertEquals("   \n   ", b.getScreenAsString());
    }

    @Test
    void getAllAsStringIncludesScrollbackAndScreen() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);
        b.write("abc");
        b.setCursor(0, 1);
        b.write("xy");
        b.insertEmptyLineAtBottom();

        assertEquals("abc\nxy \n   ", b.getAllAsString());
    }
}
