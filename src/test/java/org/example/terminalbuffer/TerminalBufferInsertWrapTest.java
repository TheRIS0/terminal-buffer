package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferInsertWrapTest {

    @Test
    void insertShiftsContentRight() {
        TerminalBuffer b = new TerminalBuffer(5, 1, 10);
        b.write("ab");
        b.setCursor(1, 0);
        b.insert("Z");
        assertEquals("aZb  ", b.getLineAsString(0));
    }

    @Test
    void insertWrapsToNextLine() {
        TerminalBuffer b = new TerminalBuffer(5, 2, 10);
        b.write("abcd");
        b.setCursor(4, 0);
        b.insert("XY");

        assertEquals("abcdX", b.getLineAsString(0));
        assertEquals("Y    ", b.getLineAsString(1));
    }

    @Test
    void insertWrapsAndScrollsWhenReachingBottom() {
        TerminalBuffer b = new TerminalBuffer(3, 2, 10);

        b.setCursor(0, 0);
        b.write("111");
        b.setCursor(0, 1);
        b.write("222");

        b.setCursor(2, 1); // last column on bottom row
        b.insert("ZZ");    // second Z should force scroll

        assertTrue(b.scrollbackSize() >= 1);
        assertEquals(2, b.height());
    }

    @Test
    void insertPreservesShiftedAttributes() {
        TerminalBuffer b = new TerminalBuffer(4, 1, 10);

        b.setCurrentAttributes((byte) 1, (byte) 1, false, false, false);
        b.write("ab");

        b.setCurrentAttributes((byte) 2, (byte) 2, true, false, false);
        b.setCursor(1, 0);
        b.insert("X");

        assertEquals("aXb ", b.getLineAsString(0));

        TextAttributes a = b.getAttributesAt(0, 0);
        TextAttributes x = b.getAttributesAt(0, 1);
        TextAttributes bAttr = b.getAttributesAt(0, 2);

        assertEquals(1, a.fg());
        assertEquals(2, x.fg());
        assertEquals(1, bAttr.fg());
    }
}
