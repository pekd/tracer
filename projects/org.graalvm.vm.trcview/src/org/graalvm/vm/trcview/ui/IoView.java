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
        channels = io.keySet().stream().mapToInt(x -> x).sorted().toArray();
        String[] names = IntStream.of(channels).mapToObj(x -> "Channel " + x).toArray(String[]::new);
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

    private static void add(StringBuilder buf, char[] s) {
        for (char val : s) {
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
                buf.append("<span class=\"unprintable\">&lt;");
                buf.append(HexFormatter.tohex(val, 2));
                buf.append("&gt;</span>");
            }
        }
    }

    private void update() {
        if (chaninput.getSelectedIndex() == -1) {
            return;
        }

        int channel = channels[chaninput.getSelectedIndex()];
        StringBuilder buf = new StringBuilder();
        buf.append("<html><head>");
        buf.append(STYLE);
        buf.append("</head><body><pre>");
        for (IoEvent evt : io.get(channel)) {
            if (evt.getStep() > insn) {
                break;
            }

            if (evt.isInput()) {
                buf.append("<span class=\"input\">");
                add(buf, evt.getValue().toCharArray());
                buf.append("</span>");
            } else {
                add(buf, evt.getValue().toCharArray());
            }
        }
        buf.append("<span class=\"cursor\">&nbsp;</span>");
        buf.append("</pre></body></html>");
        text.setText(buf.toString());
        text.setCaretPosition(text.getDocument().getLength());
    }
}
