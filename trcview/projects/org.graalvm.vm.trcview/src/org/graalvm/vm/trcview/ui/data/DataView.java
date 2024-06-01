package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.DefaultTypes;
import org.graalvm.vm.trcview.analysis.type.NameValidator;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.TraceCodeReader;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.StaticTypePropagation;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.Utils;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.trcview.ui.data.editor.JEditor;
import org.graalvm.vm.trcview.ui.event.JumpListener;
import org.graalvm.vm.trcview.ui.event.MemoryAddressListener;
import org.graalvm.vm.trcview.ui.event.StepListener;

@SuppressWarnings("serial")
public class DataView extends JPanel implements StepListener {
    private JEditor editor;
    private DataViewModel model;
    private TraceAnalyzer trc;
    private StepEvent step;

    private Deque<Long> addressStack = new ArrayDeque<>();

    public DataView(MemoryAddressListener memaddr, JumpListener jump) {
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
                            jump(addr);
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
                long addr = getOperandAddress();
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
                                mem.setName(var, name);
                                model.update();
                            } else {
                                JOptionPane.showMessageDialog(DataView.this, "Invalid name", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (input != null) {
                            mem.setName(var, null);
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
                long addr = getOperandAddress();
                ComputedSymbol sym = trc.getComputedSymbol(addr);
                if (sym != null && sym.type == ComputedSymbol.Type.SUBROUTINE) {
                    Utils.setFunctionType(sym, trc, DataView.this);
                } else {
                    Utils.setDataType(addr, trc, DataView.this);
                }
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
                    mem.set(addr, next, var.getName(trc.getArchitecture().getFormat()));
                } else {
                    mem.set(addr, getNextType(null));
                }
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke o = KeyStroke.getKeyStroke(KeyEvent.VK_O, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(o, o);
        getActionMap().put(o, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                TypedMemory mem = trc.getTypedMemory();
                Variable var = mem.get(addr);
                if (var != null) {
                    Type type = var.getType();
                    Type next = togglePointer(type);
                    mem.set(addr, next, var.getName(trc.getArchitecture().getFormat()));
                } else {
                    mem.set(addr, new Type(new Type(DataType.VOID), trc.getArchitecture().getTypeInfo()));
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
                    mem.set(addr, new Type(DataType.S8, Representation.CHAR));
                    model.update();
                }
                editor.requestFocus();
            }
        });

        KeyStroke a = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(a, a);
        getActionMap().put(a, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                TypedMemory mem = trc.getTypedMemory();
                Variable var = mem.get(addr);
                Type type = mem.findString(addr, step.getStep(), trc);
                if (type != null) {
                    if (var != null) {
                        mem.set(addr, type, var.getRawName());
                    } else {
                        mem.set(addr, type);
                    }
                    model.update();
                }
                editor.requestFocus();
            }
        });

        KeyStroke c = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(c, c);
        getActionMap().put(c, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Disassembler disasm = trc.getArchitecture().getDisassembler(trc);
                if (disasm == null) {
                    return;
                }

                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                TypedMemory mem = trc.getTypedMemory();

                int len = disasm.getLength(new TraceCodeReader(trc, addr, trc.getArchitecture().getFormat().be, step.getStep()));
                if (len == 0) {
                    return;
                }

                Variable var = mem.get(addr);
                if (var != null) {
                    trc.getTypedMemory().set(addr, null);
                }
                mem.set(addr, DefaultTypes.getCodeType(len));
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke x = KeyStroke.getKeyStroke(KeyEvent.VK_X, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(x, x);
        getActionMap().put(x, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long addr = getOperationAddress();
                XrefDialog dlg = new XrefDialog(null, trc, addr, step.getStep(), jump);
                dlg.setVisible(true);
                editor.requestFocus();
            }
        });

        KeyStroke semicolon = KeyStroke.getKeyStroke(';');
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(semicolon, semicolon);
        getActionMap().put(semicolon, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                Utils.setCommentPC(addr, trc);
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke w = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(w, w);
        getActionMap().put(w, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = editor.getCursorLine();
                long addr = model.getAddressByLine(line);
                TypedMemory mem = trc.getTypedMemory();

                Variable var = mem.get(addr);
                if (var != null) {
                    DataExportDialog dlg = new DataExportDialog(null, trc, var, step.getStep());
                    dlg.setVisible(true);
                    editor.requestFocus();
                }
            }
        });

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        getActionMap().put(esc, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addressStack.isEmpty()) {
                    return;
                } else {
                    long addr = addressStack.pop();
                    setAddress(addr);
                }
            }
        });

        KeyStroke f5 = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f5, f5);
        getActionMap().put(f5, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StaticTypePropagation prop = new StaticTypePropagation(trc);
                trc.getTypedMemory().clearDerivedTypes();
                prop.propagate(step.getStep());
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke f6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f6, f6);
        getActionMap().put(f6, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StaticTypePropagation prop = new StaticTypePropagation(trc);
                prop.propagateTypes();
                model.update();
                editor.requestFocus();
            }
        });

        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f9, f9);
        getActionMap().put(f9, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.update();
                editor.requestFocus();
            }
        });

        editor.addActionListener(e -> {
            Element element = editor.getCurrentElement();
            if (element instanceof AddressElement) {
                jump(truncptr(((AddressElement) element).getAddress()));
            } else if (element instanceof NumberElement) {
                jump(((NumberElement) element).getValue());
            } else if (element instanceof LineAddressElement) {
                memaddr.setMemoryAddress(((LineAddressElement) element).getAddress());
            }
        });
    }

    private long getOperandAddress() {
        Element element = editor.getCurrentElement();
        if (element instanceof AddressElement) {
            AddressElement ae = (AddressElement) element;
            return ae.getAddress();
        } else {
            int line = editor.getCursorLine();
            return model.getAddressByLine(line);
        }
    }

    private long getOperationAddress() {
        Element element = editor.getCurrentElement();
        if (element instanceof DataElement) {
            DataElement de = (DataElement) element;
            if (de.hasLocation()) {
                return de.getLocation();
            }
        }

        int line = editor.getCursorLine();
        return model.getAddressByLine(line);
    }

    private Representation getArchRepresentation() {
        StepFormat fmt = trc.getArchitecture().getFormat();
        return fmt.numberfmt == StepFormat.NUMBERFMT_OCT ? Representation.OCT : Representation.HEX;
    }

    public Type getNextType(Type type) {
        if (type == null) {
            Representation repr = getArchRepresentation();
            return new Type(DataType.U8, repr);
        }

        Representation repr = type.getRepresentation();
        switch (type.getType()) {
            case U8:
                return new Type(DataType.U16, type.isConst(), repr);
            case S8:
                return new Type(DataType.S16, type.isConst(), repr);
            case U16:
                return new Type(DataType.U32, type.isConst(), repr);
            case S16:
                return new Type(DataType.S32, type.isConst(), repr);
            case U32:
                return new Type(DataType.U64, type.isConst(), repr);
            case S32:
                return new Type(DataType.S64, type.isConst(), repr);
            case U64:
                return new Type(DataType.U8, type.isConst(), repr);
            case S64:
                return new Type(DataType.S8, type.isConst(), repr);
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

    public Type togglePointer(Type type) {
        ArchitectureTypeInfo info = trc.getArchitecture().getTypeInfo();
        if (type == null) {
            return new Type(new Type(DataType.VOID), info);
        }

        Representation repr = getArchRepresentation();
        if (type.getType() == DataType.PTR || type.getType() == DataType.STRING) {
            switch (info.getPointerSize()) {
                default:
                case 1:
                    return new Type(DataType.U8, repr);
                case 2:
                    return new Type(DataType.U16, repr);
                case 4:
                    return new Type(DataType.U32, repr);
                case 8:
                    return new Type(DataType.U64, repr);
            }
        } else {
            return new Type(new Type(DataType.VOID), info);
        }
    }

    private long truncptr(long ptr) {
        ArchitectureTypeInfo info = trc.getArchitecture().getTypeInfo();
        if (info != null) {
            return truncptr(ptr, info.getPointerSize());
        } else {
            return ptr;
        }
    }

    private static long truncptr(long ptr, int size) {
        switch (size) {
            case 1:
                return ptr & 0xFF;
            case 2:
                return ptr & 0xFFFF;
            case 4:
                return ptr & 0xFFFFFFFFL;
            case 8:
            default:
                return ptr;
        }
    }

    public void jump(long address) {
        long oldaddr = model.getAddressByLine(editor.getCursorLine());
        if (setAddress(address)) {
            addressStack.push(oldaddr);
        }
    }

    public boolean setAddress(long addr) {
        StepFormat fmt = trc.getArchitecture().getFormat();
        try {
            long line = model.getLineByAddress(addr);
            editor.setCursorLine((int) line);
            editor.setCursorColumn(fmt.addrwidth + 1);
            editor.scrollToCursor();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
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
