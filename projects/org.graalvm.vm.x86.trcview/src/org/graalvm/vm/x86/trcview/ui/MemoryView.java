package org.graalvm.vm.x86.trcview.ui;

import static org.graalvm.vm.x86.trcview.ui.Utils.color;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;

@SuppressWarnings("serial")
public class MemoryView extends JPanel {
    private static final int LINESZ = 16;

    private static final String STYLE = "<style>\n" +
                    "html, body, pre {\n" +
                    "    padding: 0;\n" +
                    "    margin: 0;\n" +
                    "}\n" +
                    "pre {\n" +
                    "    font-family: " + Font.MONOSPACED + ";\n" +
                    "    font-size: 11pt;\n" +
                    "}\n" +
                    ".address {\n" +
                    "    color: " + color(Color.ORANGE) + ";\n" +
                    "    background-color: " + color(Color.BLACK) + ";\n" +
                    "}\n" +
                    ".change {\n" +
                    "    color: " + color(Color.RED) + ";\n" +
                    "}\n" +
                    ".highlight {\n" +
                    "    color: " + color(Color.MAGENTA) + ";\n" +
                    "}\n" +
                    ".changeaddr {\n" +
                    "    color: " + color(Color.RED) + ";\n" +
                    "    background-color: " + color(Color.BLACK) + ";\n" +
                    "}\n" +
                    "</style>";

    private JTextPane text;
    private JFormattedTextField addrinput;
    private MemoryTrace memory;
    private long address;
    private long highlightStart;
    private long highlightEnd;
    private long insn;

    public MemoryView() {
        super(new BorderLayout());
        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/html");
        add(BorderLayout.CENTER, new JScrollPane(text));

        addrinput = new JFormattedTextField(new HexFormat());
        addrinput.addPropertyChangeListener("value", (e) -> setAddress((long) e.getNewValue()));
        int sz = addrinput.getFont().getSize();
        addrinput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, sz));
        setAddress(0);

        add(BorderLayout.NORTH, addrinput);
    }

    private static String html(char c) {
        switch (c) {
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            default:
                return Character.toString(c);
        }
    }

    public void setAddress(long address) {
        this.address = address;
        addrinput.setValue(address);
        update();
    }

    public void setHighlight(long start, long end) {
        highlightStart = start;
        highlightEnd = end;
        update();
    }

    public void setInstruction(long insn) {
        this.insn = insn;
        update();
    }

    public void setMemoryTrace(MemoryTrace memory) {
        this.memory = memory;
        update();
    }

    private static boolean isPrintable(byte value) {
        return value >= 0x20 && value <= 0x7e; // ascii
    }

    private byte getI8(long addr) throws MemoryNotMappedException {
        return memory.getByte(addr, insn);
    }

    private boolean isHighlight(long ptr) {
        return Long.compareUnsigned(ptr, highlightStart) >= 0 && Long.compareUnsigned(ptr, highlightEnd) <= 0;
    }

    private boolean isChanged(long addr) {
        try {
            long prev = insn - 1;
            if (insn == 0) {
                prev = 0;
            }
            byte val1 = memory.getByte(addr, prev);
            byte val2 = memory.getByte(addr, insn);
            return val1 != val2;
        } catch (MemoryNotMappedException e) {
            return false;
        }
    }

    private String dump(long p, int size) {
        StringBuilder buf = new StringBuilder();
        long ptr = p;
        long ptr2 = ptr;
        boolean nl = true;
        for (int i = 0; i < size; i++) {
            byte u8;
            nl = true;
            if (i % LINESZ == 0) {
                buf.append(HexFormatter.tohex(ptr, 16)).append(':');
            }
            buf.append(' ');
            if (isHighlight(ptr)) {
                buf.append("<span class=\"highlight\">");
            }
            boolean change = isChanged(ptr);
            if (change) {
                if (ptr == address) {
                    buf.append("<span class=\"changeaddr\">");
                } else {
                    buf.append("<span class=\"change\">");
                }
            } else if (ptr == address) {
                buf.append("<span class=\"address\">");
            }
            try {
                u8 = getI8(ptr);
                buf.append(HexFormatter.tohex(Byte.toUnsignedInt(u8), 2));
            } catch (MemoryNotMappedException e) {
                buf.append("--");
            }
            if (change) {
                buf.append("</span>");
            }
            if (ptr == address) {
                buf.append("</span>");
            }
            if (isHighlight(ptr)) {
                buf.append("</span>");
            }
            ptr++;
            if (i % LINESZ == (LINESZ - 1)) {
                buf.append("   ");
                for (int j = 0; j < LINESZ; j++) {
                    try {
                        u8 = getI8(ptr2);
                    } catch (MemoryNotMappedException e) {
                        u8 = '?';
                    }
                    char ch = (char) (u8 & 0xff);
                    if (!isPrintable(u8)) {
                        ch = '.';
                    }
                    if (isHighlight(ptr2)) {
                        buf.append("<span class=\"highlight\">");
                    }
                    change = isChanged(ptr2);
                    if (change) {
                        if (ptr2 == address) {
                            buf.append("<span class=\"changeaddr\">");
                        } else {
                            buf.append("<span class=\"change\">");
                        }
                    } else if (ptr2 == address) {
                        buf.append("<span class=\"address\">");
                    }
                    buf.append(html(ch));
                    if (change) {
                        buf.append("</span>");
                    }
                    if (ptr2 == address) {
                        buf.append("</span>");
                    }
                    if (isHighlight(ptr2)) {
                        buf.append("</span>");
                    }
                    ptr2++;
                }
                buf.append('\n');
                nl = false;
            }
        }
        if (nl) {
            buf.append('\n');
        }
        return buf.toString();
    }

    private void update() {
        String content = "";
        if (memory != null) {
            content = dump((address - 4 * LINESZ) & 0xFFFFFFFFFFFFFFF0L, 12 * LINESZ);
        }
        String html = "<html><head>" + STYLE + "</head><body><pre>" + content + "</pre></body></html>";
        text.setText(html);
        text.setCaretPosition(0);
    }

    private static class HexFormat extends JFormattedTextField.AbstractFormatter {
        @Override
        public Object stringToValue(String str) throws ParseException {
            try {
                return Long.parseUnsignedLong(str, 16);
            } catch (NumberFormatException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value == null || !(value instanceof Long)) {
                throw new ParseException("NULL is not a valid value", 0);
            }
            return HexFormatter.tohex((Long) value, 16);
        }
    }
}
