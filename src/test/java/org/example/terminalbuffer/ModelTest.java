package org.example.terminalbuffer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelTest {

    @Test
    void defaultAttributesAreDefault() {
        TextAttributes a = TextAttributes.defaults();
        assertEquals(TextAttributes.DEFAULT_COLOR, a.fg());
        assertEquals(TextAttributes.DEFAULT_COLOR, a.bg());
        assertEquals(0, a.styleMask());
        assertFalse(a.bold());
        assertFalse(a.italic());
        assertFalse(a.underline());
    }

    @Test
    void invalidColorThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TextAttributes((byte) 16, (byte) 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TextAttributes((byte) 0, (byte) -2, 0));
    }

    @Test
    void newLineIsEmpty() {
        Line line = new Line(5);
        assertEquals("     ", line.toPlainString());
        for (int i = 0; i < 5; i++) {
            assertEquals(Cell.EMPTY, line.cell(i).codePoint());
        }
    }

    @Test
    void deepCopyIsIndependent() {
        Line a = new Line(3);
        a.cell(0).set('A', TextAttributes.defaults());

        Line b = a.deepCopy();
        b.cell(0).set('B', TextAttributes.defaults());

        assertEquals("A  ", a.toPlainString());
        assertEquals("B  ", b.toPlainString());
    }
}
