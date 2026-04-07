package org.example.terminalbuffer;

public final class Line {
    private final int[] codePoints;
    private final TextAttributes[] attrs;

    public Line(int width) {
        if (width <= 0) throw new IllegalArgumentException("width must be > 0");
        this.codePoints = new int[width];
        this.attrs = new TextAttributes[width];
        TextAttributes def = TextAttributes.defaults();
        for (int i = 0; i < width; i++) {
            attrs[i] = def;
        }
    }

    public int width() {
        return codePoints.length;
    }

    public int codePointAt(int col) {
        return codePoints[col];
    }

    public TextAttributes attrsAt(int col) {
        return attrs[col];
    }

    public void setCell(int col, int codePoint, TextAttributes a) {
        codePoints[col] = codePoint;
        attrs[col] = TextAttributes.intern(a);
    }

    public final class CellRef {
        private final int col;

        private CellRef(int col) {
            this.col = col;
        }

        public int codePoint() {
            return codePoints[col];
        }

        public TextAttributes attrs() {
            return attrs[col];
        }

        public void set(int codePoint, TextAttributes a) {
            setCell(col, codePoint, a);
        }
    }

    public CellRef cell(int col) {
        return new CellRef(col);
    }

    public void clear() {
        TextAttributes def = TextAttributes.defaults();
        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = Cell.EMPTY;
            attrs[i] = def;
        }
    }

    public Line deepCopy() {
        Line copy = new Line(codePoints.length);
        System.arraycopy(codePoints, 0, copy.codePoints, 0, codePoints.length);
        System.arraycopy(attrs, 0, copy.attrs, 0, attrs.length);
        return copy;
    }

    public String toPlainString() {
        StringBuilder sb = new StringBuilder(codePoints.length);
        for (int cp : codePoints) {
            if (cp <= 0) {
                sb.append(' ');
            } else {
                sb.appendCodePoint(cp);
            }
        }
        return sb.toString();
    }

    public Line resizedTo(int newWidth) {
        if (newWidth <= 0) throw new IllegalArgumentException("newWidth must be > 0");
        Line out = new Line(newWidth);
        int n = Math.min(codePoints.length, newWidth);
        System.arraycopy(codePoints, 0, out.codePoints, 0, n);
        System.arraycopy(attrs, 0, out.attrs, 0, n);
        return out;
    }
}
