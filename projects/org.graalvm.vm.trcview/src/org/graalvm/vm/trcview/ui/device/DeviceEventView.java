package org.graalvm.vm.trcview.ui.device;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.event.JumpListener;
import org.graalvm.vm.trcview.ui.event.StepListener;
import org.graalvm.vm.util.io.WordOutputStream;

@SuppressWarnings("serial")
public class DeviceEventView extends JPanel implements StepListener {
    public static final Color PAST_FG = Color.BLACK;
    public static final Color FUTURE_FG = Color.GRAY;

    private JList<DeviceEvent> eventList;
    private List<DeviceEvent> events;
    private DeviceEventModel model;

    private long insn;

    private JumpListener jump;
    private TraceAnalyzer trc;
    private CellRenderer renderer;

    public DeviceEventView(JumpListener jump) {
        super(new BorderLayout());
        this.jump = jump;

        events = Collections.emptyList();
        eventList = new JList<>(model = new DeviceEventModel());
        eventList.setFont(MainWindow.FONT);
        eventList.setCellRenderer(renderer = new CellRenderer());

        add(BorderLayout.CENTER, new JScrollPane(eventList));

        eventList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jump();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        eventList.getInputMap().put(enter, enter);
        eventList.getActionMap().put(enter, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                jump();
            }
        });
    }

    private void jump() {
        DeviceEvent selected = eventList.getSelectedValue();
        if (selected != null) {
            jump.jump(trc.getInstruction(selected.getStep()));
        }
    }

    @Override
    public void setStep(StepEvent step) {
        insn = step.getStep();
        update();
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        insn = -1;
        events = Collections.emptyList();
        model.changed();
        renderer.clear();
        update();
    }

    public void setDevice(Device device) {
        events = device.getEvents();
        model.changed();
        renderer.clear();
        update();
    }

    private void update() {
        DeviceEvent target = new DeviceEvent(trc.getArchitecture().getId(), 0) {
            @Override
            public long getStep() {
                return insn;
            }

            @Override
            public int getDeviceId() {
                return 0;
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            protected void writeRecord(WordOutputStream out) throws IOException {
            }
        };

        int idx = Collections.binarySearch(events, target, (a, b) -> {
            return Long.compareUnsigned(a.getStep(), b.getStep());
        });

        if (idx < 0) {
            idx = ~idx - 1;
        }
        if (idx >= events.size()) {
            idx = events.size() - 1;
        }
        for (; idx + 1 < events.size(); idx++) {
            if (events.get(idx + 1).getStep() > insn)
                break;
        }
        if (idx >= 0) {
            eventList.setSelectedIndex(idx);
            eventList.ensureIndexIsVisible(idx);
        } else {
            eventList.clearSelection();
        }
        eventList.repaint();
    }

    protected class CellRenderer extends DefaultListCellRenderer {
        private Map<Integer, String> cache = new HashMap<>();

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            DeviceEvent evt = (DeviceEvent) value;
            String decoded = evt.getMessage();
            if (cache.containsKey(index)) {
                decoded = cache.get(index);
            } else {
                decoded = evt.getMessage();
                cache.put(index, decoded);
            }

            String comment = trc.getCommentForInsn(evt.getStep());
            if (comment != null) {
                decoded += " // " + comment;
            }

            Component c = super.getListCellRendererComponent(list, decoded, index, isSelected, cellHasFocus);
            if (evt.getStep() > insn) {
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

    public class DeviceEventModel extends AbstractListModel<DeviceEvent> {
        @Override
        public DeviceEvent getElementAt(int i) {
            return events.get(i);
        }

        @Override
        public int getSize() {
            return events.size();
        }

        public void changed() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
