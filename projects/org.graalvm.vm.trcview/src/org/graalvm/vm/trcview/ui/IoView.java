package org.graalvm.vm.trcview.ui;

import static org.graalvm.vm.trcview.ui.Utils.color;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

@SuppressWarnings("serial")
public class IoView extends JPanel {
    private static final String STYLE = "<style>\n" +
                    "html, body, pre {\n" +
                    "    padding: 0;\n" +
                    "    margin: 0;\n" +
                    "}\n" +
                    "pre {\n" +
                    "    font-family: " + Font.MONOSPACED + ";\n" +
                    "    font-size: 11pt;\n" +
                    "}\n" +
                    ".current {\n" +
                    "    color: " + color(Color.ORANGE) + ";\n" +
                    "    background-color: " + color(Color.BLACK) + ";\n" +
                    "}\n" +
                    ".unprintable {\n" +
                    "    background-color: " + color(new Color(0xE0, 0xE0, 0xE0)) + ";\n" +
                    "}\n" +
                    ".input {\n" +
                    "    color: " + color(Color.RED) + ";\n" +
                    "}\n" +
                    ".unprintable-input {\n" +
                    "    color: " + color(Color.RED) + ";\n" +
                    "    background-color: " + color(new Color(0xFF, 0xE0, 0xE0)) + ";\n" +
                    "}\n" +
                    ".cursor {\n" +
                    "    color: " + color(Color.WHITE) + ";\n" +
                    "    background-color: " + color(Color.BLACK) + ";\n" +
                    "}\n" +
                    "</style>";

    private JTextPane text;
    private JComboBox<String> chaninput;

    private Map<Integer, List<IoEvent>> io;
    private int[] channels;

    private long insn = -1;
    private long lastInsn = -1;
    private long nextInsn = -1;

    public IoView() {
        super(new BorderLayout());

        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/html");
        add(BorderLayout.CENTER, new JScrollPane(text));

        chaninput = new JComboBox<>();
        chaninput.addItemListener(e -> update());
        channels = new int[0];
        add(BorderLayout.NORTH, chaninput);
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        io = trc.getIo();
        int[] tmp = io.keySet().stream().mapToInt(x -> x).sorted().toArray();
        channels = new int[tmp.length + 1];
        channels[0] = Integer.MIN_VALUE;
        System.arraycopy(tmp, 0, channels, 1, tmp.length);

        String[] names = IntStream.of(channels).mapToObj(x -> "Channel " + x).toArray(String[]::new);
        names[0] = "None";
        chaninput.setModel(new DefaultComboBoxModel<>(names));
    }

    public void setStep(StepEvent step) {
        if (insn == step.getStep()) {
            return;
        }

        insn = step.getStep();
        update();
    }

    private static boolean isPrintable(char value) {
        return value >= 0x20 && value <= 0x7E; // ascii
    }

    private static void add(StringBuilder buf, String s, boolean input) {
        for (int i = 0; i < s.length(); i++) {
            char val = s.charAt(i);
            if (val == '\n') {
                buf.append("<br>");
            } else if (val == '\r') {
                // ignore for now
                // buf.append('\r');
            } else if (val == '\t') {
                buf.append('\t');
            } else if (isPrintable(val)) {
                switch (val) {
                    case '&':
                        buf.append("&amp;");
                        break;
                    case '<':
                        buf.append("&lt;");
                        break;
                    case '>':
                        buf.append("&gt;");
                        break;
                    default:
                        buf.append(val);
                }
            } else {
                if (input) {
                    buf.append("<span class=\"unprintable-input\">&lt;");
                } else {
                    buf.append("<span class=\"unprintable\">&lt;");
                }
                buf.append(HexFormatter.tohex(val, 2));
                buf.append("&gt;</span>");
            }
        }
    }

    private void update() {
        if (chaninput.getSelectedIndex() == -1) {
            return;
        }

        if (insn >= lastInsn && insn <= nextInsn) {
            return;
        }

        int channel = channels[chaninput.getSelectedIndex()];

        if (channel == Integer.MIN_VALUE) {
            text.setText("<html><head>" + STYLE + "</head><body></body>");
            text.setCaretPosition(0);
            return;
        }

        StringBuilder buf = new StringBuilder();
        buf.append("<html><head>");
        buf.append(STYLE);
        buf.append("</head><body><pre>");
        for (IoEvent evt : io.get(channel)) {
            if (evt.getStep() > insn) {
                nextInsn = evt.getStep();
                break;
            }

            lastInsn = evt.getStep();

            if (evt.isInput()) {
                buf.append("<span class=\"input\">");
                add(buf, evt.getValue(), true);
                buf.append("</span>");
            } else {
                add(buf, evt.getValue(), false);
            }
        }
        buf.append("<span class=\"cursor\">&nbsp;</span>");
        buf.append("</pre></body></html>");
        text.setText(buf.toString());
        text.setCaretPosition(text.getDocument().getLength());
    }
}
