package org.graalvm.vm.trcview.ui;

import static org.graalvm.vm.trcview.ui.Utils.color;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

@SuppressWarnings("serial")
public class MemoryHistoryView extends JPanel {
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
                    "    color: " + color(Color.RED) + ";\n" +
                    "}\n" +
                    "</style>";

    private JTextPane text;
    private JTextField addrinput;

    private long address;

    private long insn;
    private StepEvent step;
    private Expression expr;

    private Consumer<String> status;

    private TraceAnalyzer trc;

    public MemoryHistoryView(Consumer<String> status) {
        super(new BorderLayout());
        this.status = status;

        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/html");
        add(BorderLayout.CENTER, new JScrollPane(text));

        addrinput = new JTextField("0x" + HexFormatter.tohex(0, 16));
        int sz = addrinput.getFont().getSize();
        addrinput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, sz));
        addrinput.setForeground(Color.BLACK);
        addrinput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String s = addrinput.getText();
                Parser parser = new Parser(s.trim());
                try {
                    expr = parser.parse();
                    addrinput.setForeground(Color.BLACK);
                    status.accept("Expression parsed successfully");
                    if (eval() != address) {
                        update();
                    }
                } catch (ParseException ex) {
                    addrinput.setForeground(Color.RED);
                    status.accept("Parse error: " + ex.getMessage());
                }
            }
        });

        add(BorderLayout.NORTH, addrinput);
    }

    public String getExpression() {
        return addrinput.getText().trim();
    }

    public void setExpression(String expression) {
        addrinput.setText(expression.trim());
        Parser parser = new Parser(expression.trim());
        try {
            expr = parser.parse();
            addrinput.setForeground(Color.BLACK);
            status.accept("Expression parsed successfully");
            if (eval() != address) {
                update();
            }
        } catch (ParseException ex) {
            addrinput.setForeground(Color.RED);
            status.accept("Parse error: " + ex.getMessage());
        }
    }

    public void setStep(StepEvent step) {
        if (this.step == step) {
            return;
        }
        this.step = step;
        this.insn = step.getStep();
        update();
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        update();
    }

    private static boolean isPrintable(byte value) {
        return value >= 0x20 && value <= 0x7e; // ascii
    }

    private long eval() {
        if (expr == null) {
            return 0;
        } else if (step == null) {
            return 0;
        } else {
            ExpressionContext ctx = new ExpressionContext(step.getState(), trc);
            try {
                return expr.evaluate(ctx);
            } catch (EvaluationException e) {
                status.accept("Error evaluating expression: " + e.getMessage());
                return 0;
            }
        }
    }

    private void update() {
        address = eval();
        String content = "";
        if (trc != null) {
            try {
                List<MemoryUpdate> updates = trc.getPreviousWrites(address, insn, 8192);
                updates.sort((a, b) -> Long.compareUnsigned(a.instructionCount, b.instructionCount));
                StringBuilder buf = new StringBuilder();
                for (MemoryUpdate update : updates) {
                    byte val = update.getByte(address);
                    if (val == '\n') {
                        buf.append('\n');
                    } else if (val == '\r') {
                        buf.append('\r');
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
                                buf.append((char) Byte.toUnsignedInt(val));
                        }
                    } else {
                        buf.append("<span class=\"unprintable\">&lt;");
                        buf.append(HexFormatter.tohex(Byte.toUnsignedInt(val), 2));
                        buf.append("&gt;</span>");
                    }
                }
                content = buf.toString();
            } catch (MemoryNotMappedException e) {
                content = "<i>not mapped</i>";
            }
        }
        String html = "<html><head>" + STYLE + "</head><body><pre>" + content + "</pre></body></html>";
        text.setText(html);
        text.setCaretPosition(0);
    }
}
