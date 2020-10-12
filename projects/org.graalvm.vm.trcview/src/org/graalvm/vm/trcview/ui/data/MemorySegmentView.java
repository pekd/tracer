package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.analysis.memory.MemorySegment;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.event.StepListener;

@SuppressWarnings("serial")
public class MemorySegmentView extends JPanel implements StepListener {
    private TraceAnalyzer trc;

    private Model model;

    private JList<MemorySegment> segments;
    private List<MemorySegment> data;
    private Consumer<Long> jump;

    public MemorySegmentView(Consumer<Long> jump) {
        super(new BorderLayout());

        this.jump = jump;
        data = new ArrayList<>();
        segments = new JList<>(model = new Model());
        segments.setFont(MainWindow.FONT);
        add(BorderLayout.CENTER, new JScrollPane(segments));

        segments.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jump();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        segments.getInputMap().put(enter, enter);
        segments.getActionMap().put(enter, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                jump();
            }
        });
    }

    private void jump() {
        MemorySegment seg = segments.getSelectedValue();
        if (seg != null) {
            jump.accept(seg.getStart());
        }
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
    }

    @Override
    public void setStep(StepEvent step) {
        data = trc.getMemorySegments(step.getStep());
        model.update();
    }

    private class Model extends AbstractListModel<MemorySegment> {
        public MemorySegment getElementAt(int index) {
            return data.get(index);
        }

        public int getSize() {
            return data.size();
        }

        public void update() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
