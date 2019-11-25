package org.graalvm.vm.x86.trcview.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.OctFormatter;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.decode.DecoderUtils;
import org.graalvm.vm.x86.trcview.expression.EvaluationException;
import org.graalvm.vm.x86.trcview.expression.ExpressionContext;
import org.graalvm.vm.x86.trcview.expression.Parser;
import org.graalvm.vm.x86.trcview.expression.ast.Expression;
import org.graalvm.vm.x86.trcview.expression.ast.ValueNode;
import org.graalvm.vm.x86.trcview.io.data.StepEvent;
import org.graalvm.vm.x86.trcview.io.data.StepFormat;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class Watches extends JPanel {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);

    private static final String[] COLUMN_NAMES = {"Name", "Expression", "Format", "Value"};

    private List<Watch> watches;
    private Model model;
    private TraceAnalyzer trc;
    private StepEvent step;
    private Consumer<String> status;
    private StepFormat format;

    public Watches(Consumer<String> status) {
        super(new BorderLayout());
        this.status = status;
        watches = new ArrayList<>();

        JTable table = new JTable(model = new Model());
        table.setFont(FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(BorderLayout.CENTER, new JScrollPane(table));

        JPanel buttons = new JPanel(new FlowLayout());
        JButton addend = new JButton("E");
        JButton add = new JButton("+");
        JButton remove = new JButton("-");
        addend.addActionListener(e -> {
            int index = table.getSelectedRow();
            watches.add(new Watch("0", new ValueNode(0)));
            model.changed();
            remove.setEnabled(true);
            if (index != -1) {
                table.setRowSelectionInterval(index, index);
            }
        });
        add.addActionListener(e -> {
            int index = table.getSelectedRow();
            if (index == -1) {
                index = watches.size();
            }
            watches.add(index, new Watch("0", new ValueNode(0)));
            model.changed();
            remove.setEnabled(true);
            table.setRowSelectionInterval(index, index);
        });
        remove.addActionListener(e -> {
            if (watches.size() > 0) {
                int index = table.getSelectedRow();
                if (index == -1) {
                    index = watches.size() - 1;
                }
                watches.remove(index);
                model.changed();
                if (watches.isEmpty()) {
                    remove.setEnabled(false);
                } else {
                    if (index >= watches.size()) {
                        index--;
                    }
                    table.setRowSelectionInterval(index, index);
                }
            }
        });
        remove.setEnabled(false);
        buttons.add(add);
        buttons.add(addend);
        buttons.add(remove);
        add(BorderLayout.SOUTH, buttons);

        DefaultCellEditor cellEditor = (DefaultCellEditor) table.getDefaultEditor(Object.class);
        cellEditor.getComponent().setFont(FONT);
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        format = trc.getArchitecture().getFormat();
        model.changed();
    }

    public void setStep(StepEvent step) {
        if (this.step == step) {
            return;
        }
        this.step = step;
        model.changed();
    }

    private long eval(Expression expr) {
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

    private String bytes(long addr) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; i < 32; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, step.getStep()));
                buf.append(HexFormatter.tohex(b, 2));
                buf.append(' ');
            }
            return buf.toString().trim();
        } catch (MemoryNotMappedException e) {
            return "0x" + HexFormatter.tohex(addr);
        }
    }

    @SuppressWarnings("serial")
    private class Model extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public int getRowCount() {
            return watches.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            Watch watch = watches.get(row);
            Expression expr = watch.expr;
            switch (column) {
                case 0:
                    return watch.name;
                case 1:
                    return watch.str;
                case 2:
                    return watch.type;
                case 3:
                    long value = eval(expr);
                    if (format == null) {
                        return null;
                    }
                    if (watch.type == null) {
                        if (format.numberfmt == StepFormat.NUMBERFMT_HEX) {
                            return "0x" + HexFormatter.tohex(value) + " [" + value + "]";
                        } else if (format.numberfmt == StepFormat.NUMBERFMT_OCT) {
                            return OctFormatter.tooct(value) + " [" + value + "]";
                        } else {
                            return format.formatWord(value) + " [" + value + "]";
                        }
                    } else {
                        StringBuilder buf = new StringBuilder();
                        boolean dec = false;
                        for (char c : watch.type.toCharArray()) {
                            if (dec) {
                                switch (c) {
                                    case 'i':
                                    case 'n':
                                        buf.append(format.formatWord(value));
                                        break;
                                    case 'd':
                                        buf.append(value);
                                        break;
                                    case 'u':
                                        buf.append(Long.toUnsignedString(value));
                                        break;
                                    case 'x':
                                        buf.append(HexFormatter.tohex(value).toLowerCase());
                                        break;
                                    case 'X':
                                        buf.append(HexFormatter.tohex(value).toUpperCase());
                                        break;
                                    case 'o':
                                        buf.append(OctFormatter.tooct(value));
                                        break;
                                    case 'c':
                                        buf.append((char) value);
                                        break;
                                    case 'C':
                                        buf.append(DecoderUtils.encode((int) value & 0xFFFF));
                                        break;
                                    case 's':
                                        buf.append(DecoderUtils.cstr(value, step.getStep(), trc));
                                        break;
                                    case 'm':
                                        buf.append(bytes(value));
                                        break;
                                    default:
                                        buf.append(c);
                                        break;
                                }
                                dec = false;
                            } else {
                                if (c == '%') {
                                    dec = true;
                                } else {
                                    buf.append(c);
                                }
                            }
                        }
                        return buf.toString();
                    }
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column == 0) {
                watches.get(row).name = (String) value;
            } else if (column == 1) {
                watches.get(row).str = (String) value;
                try {
                    Expression expr = new Parser((String) value).parse();
                    watches.get(row).expr = expr;
                    changed();
                } catch (ParseException e) {
                    status.accept("Parse error: " + e.getMessage());
                    watches.get(row).expr = null;
                }
            } else if (column == 2) {
                String s = (String) value;
                if (s != null) {
                    s = s.trim();
                    if (s.length() == 0) {
                        s = null;
                    }
                }
                watches.get(row).type = s;
                changed();
            }
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        public void changed() {
            fireTableDataChanged();
        }
    }

    private static class Watch {
        public String name;
        public String str;
        public Expression expr;
        public String type;

        Watch(String str, Expression expr) {
            this.str = str;
            this.expr = expr;
        }
    }
}
