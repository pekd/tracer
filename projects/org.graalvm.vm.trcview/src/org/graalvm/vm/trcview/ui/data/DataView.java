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

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.NameValidator;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.Utils;
import org.graalvm.vm.trcview.ui.data.editor.JEditor;
import org.graalvm.vm.trcview.ui.event.StepListener;

@SuppressWarnings("serial")
public class DataView extends JPanel implements StepListener {
    private JEditor editor;
    private DataViewModel model;
    private TraceAnalyzer trc;
    private StepEvent step;

    public DataView() {
        super(new BorderLayout());
        editor = new JEditor(model = new DataViewModel());
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
                            setAddress(addr);
                        } catch (ParseException ex) {
                            JOptionPane.showMessageDialog(DataView.this, ex.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
                        } catch (EvaluationException ex) {
                            JOptionPane.showMessageDialog(DataView.this, ex.getMessage(), "Evaluation error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
                editor.requestFocus();
            }
        });

        KeyStroke n = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(n, n);
        getActionMap().put(n, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                ComputedSymbol sym = trc.getComputedSymbol(addr);
                if (sym != null) {
                    Utils.rename(sym, trc, DataView.this);
                } else {
                    TypedMemory mem = trc.getTypedMemory();
                    Variable var = mem.get(addr);
                    if (var != null) {
                        String input = JOptionPane.showInputDialog("Enter name:", var.getRawName() != null ? var.getRawName() : "");
                        if (input != null && input.trim().length() > 0) {
                            String name = input.trim();
                            if (NameValidator.isValidName(name)) {
                                var.setName(name);
                                model.update();
                            } else {
                                JOptionPane.showMessageDialog(DataView.this, "Invalid name", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            var.setName(null);
                            model.update();
                        }
                    }
                }
                editor.requestFocus();
            }
        });

        KeyStroke y = KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(y, y);
        getActionMap().put(y, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                Utils.setDataType(addr, trc, DataView.this);
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke u = KeyStroke.getKeyStroke(KeyEvent.VK_U, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(u, u);
        getActionMap().put(u, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                Variable var = trc.getTypedMemory().get(addr);
                if (var != null) {
                    trc.getTypedMemory().set(addr, null);
                }
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke d = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(d, d);
        getActionMap().put(d, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                TypedMemory mem = trc.getTypedMemory();
                Variable var = mem.get(addr);
                if (var != null) {
                    Type type = var.getType();
                    Type next = getNextType(type);
                    mem.set(addr, next, var.getName());
                } else {
                    mem.set(addr, getNextType(null));
                }
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke r = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(r, r);
        getActionMap().put(r, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                TypedMemory mem = trc.getTypedMemory();
                Variable var = mem.get(addr);
                if (var != null) {
                    Type type = var.getType();
                    if (type.getRepresentation() == Representation.CHAR) {
                        type.setRepresentation(type.getDefaultRepresentation());
                    } else {
                        type.setRepresentation(Representation.CHAR);
                    }
                    model.update();
                } else {
                    Type type = new Type(DataType.S8);
                    type.setRepresentation(Representation.CHAR);
                    mem.set(addr, type);
                    model.update();
                }
                editor.requestFocus();
            }
        });
    }

    public Type getNextType(Type type) {
        if (type == null) {
            return new Type(DataType.U8);
        }
        switch (type.getType()) {
            case U8:
                return new Type(DataType.U16, type.isConst());
            case S8:
                return new Type(DataType.S16, type.isConst());
            case U16:
                return new Type(DataType.U32, type.isConst());
            case S16:
                return new Type(DataType.S32, type.isConst());
            case U32:
                return new Type(DataType.U64, type.isConst());
            case S32:
                return new Type(DataType.S64, type.isConst());
            case U64:
                return new Type(DataType.U8, type.isConst());
            case S64:
                return new Type(DataType.S8, type.isConst());
            case FX16:
                return new Type(DataType.FX32, type.isConst());
            case FX32:
                return new Type(DataType.FX16, type.isConst());
            case F32:
                return new Type(DataType.F64, type.isConst());
            case F64:
                return new Type(DataType.F32, type.isConst());
            case PTR:
            case STRING:
                return new Type(DataType.U8, type.isConst());
            case STRUCT:
            case UNION:
            case VOID:
            case USER:
            default:
                return type;
        }
    }

    public void setAddress(long addr) {
        long line = model.getLineByAddress(addr);
        editor.setCursorLine((int) line);
        editor.scrollToCursor();
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        model.setTraceAnalyzer(trc);
    }

    @Override
    public void setStep(StepEvent step) {
        this.step = step;
        model.setStep(step.getStep());
    }
}
