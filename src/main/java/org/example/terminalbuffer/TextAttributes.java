package org.example.terminalbuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TextAttributes {
    public static final byte DEFAULT_COLOR = -1;

    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINE = 4;

    private static final Map<Long, TextAttributes> INTERN = new HashMap<>();

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

    private static long pack(byte fg, byte bg, int styleMask) {
        return (fg & 0xFFL) | ((bg & 0xFFL) << 8) | ((long) styleMask << 16);
    }

    public static TextAttributes intern(byte fg, byte bg, int styleMask) {
        validateColor(fg);
        validateColor(bg);
        long key = pack(fg, bg, styleMask);
        TextAttributes existing = INTERN.get(key);
        if (existing != null) {
            return existing;
        }
        TextAttributes nu = new TextAttributes(fg, bg, styleMask);
        INTERN.put(key, nu);
        return nu;
    }

    public static TextAttributes intern(TextAttributes a) {
        Objects.requireNonNull(a);
        return intern(a.fg, a.bg, a.styleMask);
    }

    public static TextAttributes defaults() {
        return intern(DEFAULT_COLOR, DEFAULT_COLOR, 0);
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
