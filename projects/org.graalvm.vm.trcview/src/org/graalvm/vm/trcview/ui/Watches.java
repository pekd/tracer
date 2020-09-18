package org.graalvm.vm.trcview.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.info.Formatter;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

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
                    return Formatter.format(watch.type, format, step.getStep(), trc, value);
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
                    Expression expr = new Parser((String) value).parseExpression();
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

    public List<Watch> getWatches() {
        return Collections.unmodifiableList(watches);
    }

    public void setWatches(List<Watch> watches) {
        this.watches = new ArrayList<>(watches);
        model.changed();
    }

    public static class Watch {
        public String name;
        public String str;
        public Expression expr;
        public String type;

        public Watch(String name, String type, String str, Expression expr) {
            this.name = name;
            this.type = type;
            this.str = str;
            this.expr = expr;
        }

        public Watch(String str, Expression expr) {
            this.str = str;
            this.expr = expr;
        }
    }
}
