package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferSkeletonTest {

    @Test
    void constructorSetsDefaults() {
        TerminalBuffer b = new TerminalBuffer(5, 3, 10);
        assertEquals(5, b.width());
        assertEquals(3, b.height());
        assertEquals(10, b.scrollbackMax());
        assertEquals(0, b.scrollbackSize());
        assertEquals(0, b.cursorCol());
        assertEquals(0, b.cursorRow());
        assertEquals(TextAttributes.defaults(), b.currentAttributes());
    }

    @Test
    void constructorValidatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(0, 3, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(5, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new TerminalBuffer(5, 3, -1));
    }

    @Test
    void cursorClamps() {
        TerminalBuffer b = new TerminalBuffer(5, 3, 10);
        b.setCursor(999, 999);
        assertEquals(4, b.cursorCol());
        assertEquals(2, b.cursorRow());
        b.setCursor(-10, -10);
        assertEquals(0, b.cursorCol());
        assertEquals(0, b.cursorRow());
    }

    @Test
    void cursorMovesClamped() {
        TerminalBuffer b = new TerminalBuffer(5, 3, 10);
        b.setCursor(2, 1);

        b.moveCursorRight(100);
        assertEquals(4, b.cursorCol());

        b.moveCursorLeft(100);
        assertEquals(0, b.cursorCol());

        b.moveCursorDown(100);
        assertEquals(2, b.cursorRow());

        b.moveCursorUp(100);
        assertEquals(0, b.cursorRow());
    }

    @Test
    void negativeCursorMovesThrow() {
        TerminalBuffer b = new TerminalBuffer(5, 3, 10);
        assertThrows(IllegalArgumentException.class, () -> b.moveCursorRight(-1));
        assertThrows(IllegalArgumentException.class, () -> b.moveCursorDown(-5));
    }

    @Test
    void attributesCanBeSetAndReset() {
        TerminalBuffer b = new TerminalBuffer(5, 3, 10);

        b.setCurrentAttributes((byte) 1, (byte) 2, true, false, true);
        TextAttributes a = b.currentAttributes();
        assertEquals(1, a.fg());
        assertEquals(2, a.bg());
        assertTrue(a.bold());
        assertTrue(a.underline());
        assertFalse(a.italic());

        b.resetAttributes();
        assertEquals(TextAttributes.defaults(), b.currentAttributes());
    }
}
