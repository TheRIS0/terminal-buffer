package org.example.terminalbuffer;

import java.util.Objects;

public final class TextAttributes {
    public static final byte DEFAULT_COLOR = -1;

    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINE = 4;

    private final byte fg;
    private final byte bg;
    private final int styleMask;

    public TextAttributes(byte fg, byte bg, int styleMask) {
        validateColor(fg);
        validateColor(bg);
        this.fg = fg;
        this.bg = bg;
        this.styleMask = styleMask;
    }

    public static TextAttributes defaults() {
        return new TextAttributes(DEFAULT_COLOR, DEFAULT_COLOR, 0);
    }

    public byte fg() {
        return fg;
    }

    public byte bg() {
        return bg;
    }

    public int styleMask() {
        return styleMask;
    }

    public boolean bold() {
        return (styleMask & BOLD) != 0;
    }

    public boolean italic() {
        return (styleMask & ITALIC) != 0;
    }

    public boolean underline() {
        return (styleMask & UNDERLINE) != 0;
    }

    public static void validateColor(byte color) {
        if (color == DEFAULT_COLOR) return;
        if (color < 0 || color > 15) {
            throw new IllegalArgumentException("Color must be default(-1) or 0..15, got: " + color);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TextAttributes other)) return false;
        return fg == other.fg && bg == other.bg && styleMask == other.styleMask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fg, bg, styleMask);
    }
}
