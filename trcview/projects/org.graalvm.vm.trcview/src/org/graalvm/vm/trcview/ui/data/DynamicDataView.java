package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.StepListener;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;

@SuppressWarnings("serial")
public class DynamicDataView extends JPanel implements StepListener {
    private TraceAnalyzer trc;
    private StepEvent step;
    private JTextField addrinput;
    private Expression expr;
    private long address;

    private JTextPane text;

    private Consumer<String> status;

    public DynamicDataView(TraceAnalyzer trc, Consumer<String> status) {
        super(new BorderLayout());

        this.status = status;

        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/plain");
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
                    expr = parser.parseExpression();
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

        setTraceAnalyzer(trc);
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        update();
    }

    public void setStep(StepEvent step) {
        this.step = step;
        update();
    }

    public void setExpression(String expression) {
        addrinput.setText(expression.trim());
        Parser parser = new Parser(expression.trim());
        try {
            expr = parser.parseExpression();
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

        long stepid = 0;
        if (step != null) {
            stepid = step.getStep();
        }

        if (trc.getTypeRecovery() == null) {
            return;
        }

        Semantics semantics = trc.getTypeRecovery().getSemantics();
        if (semantics == null) {
            return;
        }

        StepFormat format = trc.getArchitecture().getFormat();

        StringBuilder buf = new StringBuilder();
        int ptrSize = trc.getArchitecture().getTypeInfo().getPointerSize();
        for (int i = 0; i < 16;) {
            long addr = address + i;

            long flags = semantics.resolveMemory(addr, stepid);
            if ((flags & VariableType.BIT_MASK) == 0) {
                buf.append(format.formatAddress(addr)).append(": ???\n");
                i++;
            } else {
                VariableType type = VariableType.resolve(flags, ptrSize);
                int size = type.getSize(ptrSize);
                assert size > 0;

                buf.append(format.formatAddress(addr)).append(": ").append(type);

                if (type.equals(VariableType.UNKNOWN)) {
                    buf.append(" [");
                    boolean first = true;
                    for (VariableType t : VariableType.getTypeConstraints()) {
                        if (BitTest.test(flags, t.getMask())) {
                            if (!first) {
                                buf.append(' ');
                            } else {
                                first = false;
                            }
                            buf.append(t.getName());
                        }
                    }
                    buf.append("]");
                }
                buf.append('\n');

                i += size;
            }
        }
        text.setText(buf.toString());
        text.setCaretPosition(0);
    }
}
