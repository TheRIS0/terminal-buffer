package org.example.terminalbuffer;

public class DemoMain {
    public static void main(String[] args) {
        TerminalBuffer b = new TerminalBuffer(10, 4, 5);

        System.out.println("== Initial screen ==");
        System.out.println(b.getScreenAsString());
        System.out.println();

        b.write("Hello");
        b.setCursor(0, 1);
        b.write("World");

        System.out.println("== After write ==");
        System.out.println(b.getScreenAsString());
        System.out.println();

        b.setCursor(3, 0);
        b.insert("X");

        System.out.println("== After insert at (3,0) ==");
        System.out.println(b.getScreenAsString());
        System.out.println();

        b.insertEmptyLineAtBottom();
        b.insertEmptyLineAtBottom();

        System.out.println("== After scrolling (2x) ==");
        System.out.println("Scrollback+Screen:");
        System.out.println(b.getAllAsString());
        System.out.println();

        String emoji = new String(Character.toChars(0x1F600));
        b.setCursor(8, 3);
        b.write(emoji);

        System.out.println("== After wide char write near EOL ==");
        System.out.println(b.getScreenAsString());
        System.out.println();

        b.resize(12, 3);

        System.out.println("== After resize to 12x3 ==");
        System.out.println(b.getScreenAsString());
    }
}
