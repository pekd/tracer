package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.JEditor;
import org.graalvm.vm.trcview.ui.event.StepListener;

@SuppressWarnings("serial")
public class DataView extends JPanel implements StepListener {
    private DataViewModel model;
    private TraceAnalyzer trc;
    private StepEvent step;

    public DataView() {
        super(new BorderLayout());
        JEditor editor = new JEditor(model = new DataViewModel());
        add(BorderLayout.CENTER, new JScrollPane(editor));

        KeyStroke g = KeyStroke.getKeyStroke(KeyEvent.VK_G, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(g, g);
        getActionMap().put(g, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog("Enter address:");
                if (input != null) {
                    input = input.trim();
                    if (input.length() != 0) {
                        try {
                            Parser p = new Parser(input);
                            Expression expr = p.parseExpression();
                            ExpressionContext ctx = new ExpressionContext(step.getState(), trc);
                            long addr = expr.evaluate(ctx);
                            long line = model.getLineByAddress(addr);
                            editor.setCursorLine((int) line);
                            editor.scrollToCursor();
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(DataView.this, ex.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
                        } catch (EvaluationException ex) {
                            JOptionPane.showMessageDialog(DataView.this, ex.getMessage(), "Evaluation error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        model.setTraceAnalyzer(trc);
    }

    public void setStep(StepEvent step) {
        this.step = step;
        model.setStep(step.getStep());
    }
}
