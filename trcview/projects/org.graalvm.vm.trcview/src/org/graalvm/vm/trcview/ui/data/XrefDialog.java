package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.event.JumpListener;
import org.graalvm.vm.util.StringUtils;

@SuppressWarnings("serial")
public class XrefDialog extends JDialog {
    private final TraceAnalyzer trc;
    private final JumpListener jump;
    private JList<Xref> xrefs;
    private int stepwidth = 0;

    public XrefDialog(JFrame owner, TraceAnalyzer trc, long addr, long step, JumpListener jump) {
        super(owner, "Xrefs for address " + trc.getArchitecture().getFormat().formatAddress(addr), false);
        this.trc = trc;
        this.jump = jump;

        Xref[] data = getXrefs(addr);
        xrefs = new JList<>(data);
        if (data.length > 1000) {
            xrefs.setPrototypeCellValue(new Xref(null, false, -1));
        }

        if (data.length > 0) {
            int idx = Arrays.binarySearch(data, new Xref(null, false, step));
            if (idx < 0) {
                int i = ~idx - 1;
                if (i >= data.length) {
                    xrefs.setSelectedIndex(data.length - 1);
                } else if (i < 0) {
                    xrefs.setSelectedIndex(0);
                } else {
                    xrefs.setSelectedIndex(i);
                }
            } else {
                xrefs.setSelectedIndex(idx);
            }
        }

        xrefs.setFont(MainWindow.FONT);
        xrefs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jump();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        xrefs.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, enter);
        xrefs.getActionMap().put(enter, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jump();
            }
        });

        JButton ok = new JButton("Goto");
        ok.addActionListener(e -> jump());

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(ok);
        buttons.add(close);

        JPanel content = new JPanel(new BorderLayout());
        content.add(BorderLayout.CENTER, new JScrollPane(xrefs));
        content.add(BorderLayout.SOUTH, buttons);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        content.getActionMap().put(esc, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setContentPane(content);

        setSize(640, 480);
        setLocationRelativeTo(owner);

        validate();

        xrefs.scrollRectToVisible(xrefs.getVisibleRect());
    }

    private void jump() {
        Xref xref = xrefs.getSelectedValue();
        if (xref == null) {
            return;
        }

        // perform jump
        StepEvent step = xref.getStep();
        if (step == null) {
            long time = xref.getTime();
            if (time == 0) {
                jump.jump(trc.getRoot());
                dispose();
            }
        } else {
            jump.jump(step);
            dispose();
        }
    }

    private Xref[] getXrefs(long addr) {
        try {
            List<MemoryRead> reads = trc.getReadXrefs(addr);
            List<MemoryUpdate> writes = trc.getWriteXrefs(addr);

            Xref[] result = new Xref[reads.size() + writes.size()];
            int i = 0;
            for (MemoryRead read : reads) {
                result[i++] = new Xref(read.step, false, read.instructionCount);
            }
            for (MemoryUpdate write : writes) {
                result[i++] = new Xref(write.step, true, write.instructionCount);
            }

            assert i == result.length;
            Arrays.sort(result);

            if (result.length > 0) {
                long last = result[result.length - 1].getTime();
                double digits = Math.ceil(Math.log(last) / Math.log(10));
                stepwidth = (int) Math.round(digits);
            } else {
                stepwidth = 1;
            }

            return result;
        } catch (MemoryNotMappedException e) {
            return new Xref[0];
        }
    }

    private class Xref implements Comparable<Xref> {
        private final StepEvent step;
        private final long time;
        private final boolean write;

        public Xref(StepEvent step, boolean write, long time) {
            this.step = step;
            this.write = write;
            this.time = time;
        }

        public StepEvent getStep() {
            return step;
        }

        public long getTime() {
            return time;
        }

        @Override
        public String toString() {
            if (time == -1) {
                // special case: JList prototype value for component width
                String stepcnt = StringUtils.repeat("0", stepwidth);
                return "W " + trc.getArchitecture().getFormat().formatAddress(0) + " [step " + stepcnt + "] => mov rax, rdi";
            }

            String rw = write ? "W" : "R";
            if (step == null) {
                return rw + " (unknown, step " + time + ")";
            } else {
                long pc = step.getPC();
                String disasm = step.getDisassembly().replace("\t", " ");
                String stepcnt = Long.toUnsignedString(time);
                if (stepcnt.length() < stepwidth) {
                    stepcnt = StringUtils.repeat(" ", stepwidth - stepcnt.length()) + stepcnt;
                }
                return rw + " " + trc.getArchitecture().getFormat().formatAddress(pc) + " [step " + stepcnt + "] => " + disasm;
            }
        }

        public int compareTo(Xref o) {
            if (o == null) {
                return -1;
            }
            return Long.compareUnsigned(time, o.time);
        }
    }
}
