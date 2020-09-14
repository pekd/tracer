package org.graalvm.vm.trcview.ui.call;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.LabeledPairLayout;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

@SuppressWarnings("serial")
public class SyscallCallingConventionEditor extends JPanel {
    private static final Logger log = Trace.create(SyscallCallingConventionEditor.class);

    private static final String[] COLUMN_NAMES = {"ID", "Expression"};

    private JTextField syscallId;
    private JTextField stackArguments;
    private JTextField returnValue;
    private GenericABI abi;

    private List<String> expressions = new ArrayList<>();
    private Model argmodel;

    public SyscallCallingConventionEditor() {
        super(new BorderLayout());

        JPanel north = new JPanel(new LabeledPairLayout());
        north.add(LabeledPairLayout.LABEL, new JLabel("Syscall ID"));
        north.add(LabeledPairLayout.COMPONENT, syscallId = new JTextField("0"));
        north.add(LabeledPairLayout.LABEL, new JLabel("Return Value"));
        north.add(LabeledPairLayout.COMPONENT, returnValue = new JTextField("0"));
        north.add(LabeledPairLayout.LABEL, new JLabel("Extra Arguments"));
        north.add(LabeledPairLayout.COMPONENT, stackArguments = new JTextField(""));
        add(BorderLayout.NORTH, north);

        add(BorderLayout.NORTH, north);

        add(BorderLayout.CENTER, new JScrollPane(new JTable(argmodel = new Model())));

        JPanel south = new JPanel(new FlowLayout());
        JButton add = new JButton("+");
        JButton remove = new JButton("-");
        south.add(add);
        south.add(remove);
        add(BorderLayout.SOUTH, south);

        add.addActionListener(e -> {
            expressions.add("0");
            argmodel.update();
        });

        remove.addActionListener(e -> {
            if (!expressions.isEmpty()) {
                expressions.remove(expressions.size() - 1);
                argmodel.update();
            }
        });

        syscallId.setForeground(Color.BLACK);
        syscallId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String s = syscallId.getText();
                Parser parser = new Parser(s.trim());
                try {
                    parser.parse();
                    syscallId.setForeground(Color.BLACK);
                } catch (ParseException ex) {
                    syscallId.setForeground(Color.RED);
                }
            }
        });

        returnValue.setForeground(Color.BLACK);
        returnValue.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String s = returnValue.getText();
                Parser parser = new Parser(s.trim());
                try {
                    parser.parse();
                    returnValue.setForeground(Color.BLACK);
                } catch (ParseException ex) {
                    returnValue.setForeground(Color.RED);
                }
            }
        });

        stackArguments.setForeground(Color.BLACK);
        stackArguments.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String s = stackArguments.getText();
                Parser parser = new Parser(s.trim());
                try {
                    parser.parse();
                    stackArguments.setForeground(Color.BLACK);
                } catch (ParseException ex) {
                    stackArguments.setForeground(Color.RED);
                }
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        ABI a = trc.getABI();
        if (a instanceof GenericABI) {
            abi = (GenericABI) a;
            returnValue.setEditable(true);
            stackArguments.setEditable(true);
            update();
        } else {
            abi = null;
            returnValue.setEditable(false);
            stackArguments.setEditable(false);
        }
    }

    private void update() {
        Expression id = abi.getSyscallId();
        if (id != null) {
            syscallId.setText(id.toString());
        } else {
            syscallId.setText("");
        }

        Expression retn = abi.getSyscall().getReturn();
        if (retn != null) {
            returnValue.setText(retn.toString());
        } else {
            returnValue.setText("");
        }

        Expression stack = abi.getSyscall().getStack();
        if (stack != null) {
            stackArguments.setText(stack.toString());
        } else {
            stackArguments.setText("");
        }

        expressions.clear();
        for (Expression expr : abi.getSyscall().getArguments()) {
            expressions.add(expr.toString());
        }
        argmodel.update();
    }

    public boolean check() {
        String idexpr = syscallId.getText().trim();
        if (!idexpr.isEmpty()) {
            Parser parser = new Parser(idexpr);
            try {
                parser.parse();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Parse error: " + e.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        String retexpr = returnValue.getText().trim();
        if (!retexpr.isEmpty()) {
            Parser parser = new Parser(retexpr);
            try {
                parser.parse();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Parse error: " + e.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        String stackexpr = stackArguments.getText().trim();
        if (!stackexpr.isEmpty()) {
            Parser parser = new Parser(stackexpr);
            try {
                parser.parse();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Parse error: " + e.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        for (String expr : expressions) {
            Parser parser = new Parser(expr);
            try {
                parser.parse();
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Cannot parse expression \"" + expr + "\": " + e.getMessage(), "Parse error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    public void commit() {
        String idexpr = syscallId.getText().trim();
        if (idexpr.isEmpty()) {
            abi.setSyscallId(null);
        } else {
            try {
                Expression id = new Parser(idexpr).parse();
                abi.setSyscallId(id);
            } catch (ParseException e) {
                log.log(Levels.WARNING, "Cannot parse expression \"" + idexpr + "\": " + e.getMessage(), e);
            }
        }

        String retexpr = returnValue.getText().trim();
        if (retexpr.isEmpty()) {
            abi.getSyscall().setReturn(null);
        } else {
            try {
                Expression retn = new Parser(retexpr).parse();
                abi.getSyscall().setReturn(retn);
            } catch (ParseException e) {
                log.log(Levels.WARNING, "Cannot parse expression \"" + retexpr + "\": " + e.getMessage(), e);
            }
        }

        String stackexpr = stackArguments.getText().trim();
        if (stackexpr.isEmpty()) {
            abi.getSyscall().setStack(null);
        } else {
            try {
                Expression stack = new Parser(stackexpr).parse();
                abi.getSyscall().setStack(stack);
            } catch (ParseException e) {
                log.log(Levels.WARNING, "Cannot parse expression \"" + stackexpr + "\": " + e.getMessage(), e);
            }
        }

        List<Expression> args = new ArrayList<>();
        for (String expr : expressions) {
            try {
                args.add(new Parser(expr).parse());
            } catch (ParseException e) {
                log.log(Levels.WARNING, "Cannot parse expression \"" + expr + "\": " + e.getMessage(), e);
                args.add(new ValueNode(0));
            }
        }
        abi.getSyscall().setArguments(args);
    }

    private class Model extends AbstractTableModel {
        @Override
        public String getColumnName(int col) {
            return COLUMN_NAMES[col];
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return expressions.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return Integer.toString(row + 1);
            } else {
                return expressions.get(row);
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 1) {
                expressions.set(row, (String) value);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        public void update() {
            fireTableDataChanged();
        }
    }
}
