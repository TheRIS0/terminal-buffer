package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferBasicEditingTest {

    @Test
    void writeOverwritesAndMovesCursor() {
        TerminalBuffer b = new TerminalBuffer(5, 2, 10);
        b.write("abc");
        assertEquals("abc  ", b.getLineAsString(0));
        assertEquals(3, b.cursorCol());
        assertEquals(0, b.cursorRow());
    }

    @Test
    void writeStopsAtEndOfLine() {
        TerminalBuffer b = new TerminalBuffer(5, 1, 10);
        b.setCursor(4, 0);
        b.write("XYZ");
        assertEquals("    X", b.getLineAsString(0));
        assertEquals(4, b.cursorCol());
    }

    @Test
    void writeUsesCurrentAttributes() {
        TerminalBuffer b = new TerminalBuffer(5, 1, 10);
        b.setCurrentAttributes((byte) 1, (byte) 2, true, false, true);
        b.write("a");

        assertEquals('a', b.getCodePointAt(0, 0));
        TextAttributes a = b.getAttributesAt(0, 0);
        assertEquals(1, a.fg());
        assertEquals(2, a.bg());
        assertTrue(a.bold());
        assertTrue(a.underline());
        assertFalse(a.italic());
    }

    @Test
    void fillLineFillsWithCurrentAttributes() {
        TerminalBuffer b = new TerminalBuffer(4, 2, 10);
        b.setCurrentAttributes((byte) 3, (byte) 4, false, true, false);
        b.fillLine(1, 'X');

        assertEquals("XXXX", b.getLineAsString(1));
        TextAttributes a = b.getAttributesAt(1, 0);
        assertEquals(3, a.fg());
        assertEquals(4, a.bg());
        assertTrue(a.italic());
    }

    @Test
    void getScreenAsStringReturnsAllRows() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);
        b.write("abc");
        b.setCursor(0, 1);
        b.write("xy");
        assertEquals("abc\nxy ", b.getScreenAsString());
    }
}
