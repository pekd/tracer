package org.graalvm.vm.trcview.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.JumpListener;

@SuppressWarnings("serial")
public class StraceView extends JPanel {
    public static final Color PAST_FG = Color.BLACK;
    public static final Color FUTURE_FG = Color.GRAY;

    private JList<Node> strace;
    private StraceModel model;
    private List<Node> syscalls;

    private long insn;

    private JumpListener jump;
    private TraceAnalyzer trc;
    private CellRenderer renderer;

    public StraceView(JumpListener jump) {
        super(new BorderLayout());
        this.jump = jump;

        syscalls = Collections.emptyList();
        strace = new JList<>(model = new StraceModel());
        strace.setFont(MainWindow.FONT);
        strace.setCellRenderer(renderer = new CellRenderer());

        add(BorderLayout.CENTER, new JScrollPane(strace));

        strace.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jump();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        strace.getInputMap().put(enter, enter);
        strace.getActionMap().put(enter, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                jump();
            }
        });
    }

    private void jump() {
        Node selected = strace.getSelectedValue();
        if (selected != null) {
            jump.jump(selected);
        }
    }

    public void setStep(StepEvent step) {
        insn = step.getStep();
        update();
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        insn = -1;
        syscalls = trc.getSyscalls();
        model.changed();
        renderer.clear();
        update();
    }

    public void refresh() {
        renderer.clear();
        repaint();
    }

    private static StepEvent step(Node node) {
        if (node instanceof StepEvent) {
            return (StepEvent) node;
        } else if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            if (block.isInterrupt()) {
                return block.getFirstStep();
            } else {
                return block.getHead();
            }
        } else {
            throw new IllegalArgumentException("not a StepEvent/BlockNode");
        }
    }

    private void update() {
        Node target = new StepEvent(0) {
            @Override
            public byte[] getMachinecode() {
                return null;
            }

            @Override
            public String[] getDisassemblyComponents() {
                return null;
            }

            @Override
            public String getMnemonic() {
                return null;
            }

            @Override
            public long getPC() {
                return 0;
            }

            @Override
            public InstructionType getType() {
                return null;
            }

            @Override
            public long getStep() {
                return insn;
            }

            @Override
            public CpuState getState() {
                return null;
            }

            @Override
            public StepFormat getFormat() {
                return null;
            }
        };

        int idx = Collections.binarySearch(syscalls, target, (a, b) -> {
            return Long.compareUnsigned(step(a).getStep(), step(b).getStep());
        });

        if (idx < 0) {
            idx = ~idx - 1;
        }
        if (idx >= syscalls.size()) {
            idx = syscalls.size() - 1;
        }
        if (idx >= 0) {
            strace.setSelectedIndex(idx);
            strace.ensureIndexIsVisible(idx);
        } else {
            strace.clearSelection();
        }
        strace.repaint();
    }

    protected class CellRenderer extends DefaultListCellRenderer {
        private Map<Integer, String> cache = new HashMap<>();

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String decoded;
            StepEvent step = step((Node) value);
            if (cache.containsKey(index)) {
                decoded = cache.get(index);
            } else {
                Node nn = trc.getNextStep((Node) value);
                StepEvent next = null;
                if (nn instanceof StepEvent) {
                    next = (StepEvent) nn;
                } else if (nn instanceof BlockNode) {
                    next = ((BlockNode) nn).getHead();
                    if (next == null) {
                        next = ((BlockNode) nn).getFirstStep();
                    }
                }
                CpuState ns = next == null ? null : next.getState();
                decoded = trc.getArchitecture().getSyscallDecoder().decode(step.getState(), ns, trc);
                if (decoded == null) {
                    decoded = step.getDisassembly().replace('\t', ' ');
                }
                cache.put(index, decoded);
            }

            String comment1 = trc.getCommentForPC(step.getPC());
            String comment2 = trc.getCommentForInsn(step.getStep());
            String comment = comment1;
            if (comment != null) {
                if (comment2 != null) {
                    comment += "; " + comment2;
                }
            } else {
                comment = comment2;
            }
            if (comment != null) {
                decoded += " // " + comment;
            }

            Component c = super.getListCellRendererComponent(list, decoded, index, isSelected, cellHasFocus);
            if (step.getStep() > insn) {
                c.setForeground(FUTURE_FG);
            } else {
                c.setForeground(PAST_FG);
            }
            return c;
        }

        public void clear() {
            cache.clear();
        }
    }

    public class StraceModel extends AbstractListModel<Node> {
        @Override
        public Node getElementAt(int i) {
            return syscalls.get(i);
        }

        @Override
        public int getSize() {
            return syscalls.size();
        }

        public void changed() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
