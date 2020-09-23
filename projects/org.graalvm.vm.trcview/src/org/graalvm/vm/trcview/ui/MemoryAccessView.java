package org.graalvm.vm.trcview.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.HexFormatter;

@SuppressWarnings("serial")
public class MemoryAccessView extends JPanel {
    public static final Color READ_BG = new Color(0xE0, 0xFF, 0xE0);
    public static final Color WRITE_BG = new Color(0xFF, 0xE0, 0xE0);

    private MemoryView mem;

    private List<MemoryEvent> accesses;

    private final JList<MemoryEvent> memory;
    private final MemoryModel model;

    public MemoryAccessView(MemoryView mem) {
        super(new BorderLayout());
        this.mem = mem;

        accesses = new ArrayList<>();
        memory = new JList<>(model = new MemoryModel());
        memory.setFont(MainWindow.FONT);
        memory.setCellRenderer(new CellRenderer());

        add(BorderLayout.CENTER, new JScrollPane(memory));

        memory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jump();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        memory.getInputMap().put(enter, enter);
        memory.getActionMap().put(enter, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                jump();
            }
        });
    }

    private void jump() {
        MemoryEvent selected = memory.getSelectedValue();
        if (selected != null) {
            mem.setExpression("0x" + HexFormatter.tohex(selected.getAddress()));
        }
    }

    public void setStep(StepEvent step) {
        accesses.clear();

        // reads
        MemoryEvent evt = step.getRead();
        while (evt != null) {
            accesses.add(evt);
            evt = evt.getNext();
        }

        // writes
        evt = step.getWrite();
        while (evt != null) {
            accesses.add(evt);
            evt = evt.getNext();
        }

        model.changed();
        memory.clearSelection();
    }

    public void refresh() {
        repaint();
    }

    protected class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            MemoryEvent evt = (MemoryEvent) value;
            String decoded = evt.info();
            Component c = super.getListCellRendererComponent(list, decoded, index, isSelected, cellHasFocus);
            if (!isSelected) {
                c.setBackground(evt.isWrite() ? WRITE_BG : READ_BG);
            }
            return c;
        }
    }

    public class MemoryModel extends AbstractListModel<MemoryEvent> {
        @Override
        public MemoryEvent getElementAt(int i) {
            return accesses.get(getSize() - i - 1);
        }

        @Override
        public int getSize() {
            return accesses.size();
        }

        public void changed() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
