package org.graalvm.vm.trcview.ui.call;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.expression.TypeParser;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class SyscallEditor extends JPanel {
    private static final String[] COLUMN_NAMES = {"ID", "Prototype"};

    private GenericABI abi;

    private Model model;

    private List<Syscall> syscalls = new ArrayList<>();

    private static class Syscall {
        public long id;
        public Function func;

        Syscall(long id, Function func) {
            this.id = id;
            this.func = func;
        }
    }

    public SyscallEditor() {
        super(new BorderLayout());

        JTable table = new JTable(model = new Model());
        add(BorderLayout.CENTER, new JScrollPane(table));

        JPanel south = new JPanel(new FlowLayout());
        JButton add = new JButton("+");
        JButton remove = new JButton("-");
        south.add(add);
        south.add(remove);
        add(BorderLayout.SOUTH, south);

        add.addActionListener(e -> {
            Prototype type = new Prototype();
            syscalls.add(new Syscall(0, new Function("syscall", type)));
            model.update();
        });

        remove.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (!syscalls.isEmpty() && selected != -1) {
                syscalls.remove(selected);
                model.update();
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        ABI a = trc.getABI();
        if (a instanceof GenericABI) {
            abi = (GenericABI) a;
            update();
        } else {
            abi = null;
        }
    }

    private void update() {
        syscalls.clear();
        for (Entry<Long, Function> entry : abi.getSyscalls().entrySet()) {
            long scid = entry.getKey();
            Function func = entry.getValue();
            syscalls.add(new Syscall(scid, func));
        }
        syscalls.sort((a, b) -> Long.compareUnsigned(a.id, b.id));
        model.update();
    }

    public boolean check() {
        return true;
    }

    public void commit() {
        Map<Long, Function> scmap = new HashMap<>();
        for (Syscall sc : syscalls) {
            scmap.put(sc.id, sc.func);
        }
        abi.setSyscalls(scmap);
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
            return syscalls.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return syscalls.get(row).id;
            } else {
                return syscalls.get(row).func.toString();
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                long oldId = syscalls.get(row).id;
                long newId = Long.parseLong((String) value);
                if (oldId != newId) {
                    syscalls.get(row).id = newId;
                    syscalls.sort((a, b) -> Long.compareUnsigned(a.id, b.id));
                    update();
                }
            } else if (col == 1) {
                long id = syscalls.get(row).id;
                TypeParser parser = new TypeParser((String) value);
                try {
                    Function func = parser.parse();
                    Syscall sc = new Syscall(id, func);
                    syscalls.set(row, sc);
                } catch (ParseException e) {
                    // nothing for now
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void update() {
            fireTableDataChanged();
        }
    }
}
