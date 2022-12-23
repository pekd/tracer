package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.StepListenable;
import org.graalvm.vm.trcview.ui.event.TraceListenable;
import org.graalvm.vm.trcview.ui.event.TraceListener;

@SuppressWarnings("serial")
public class DataDialog extends JDialog {
    private DataView data;
    private DatatypeView types;
    private NameView names;
    private MemorySegmentView segments;
    private DynamicDataView dynamic;

    public DataDialog(JFrame owner, TraceAnalyzer trc, StepListenable step, TraceListenable trace) {
        super(owner, "Data", false);

        setLayout(new BorderLayout());

        JLabel status = new JLabel("Ready");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Data", data = new DataView());
        tabs.addTab("Segments", segments = new MemorySegmentView(addr -> {
            data.setAddress(addr);
            tabs.setSelectedIndex(0);
        }));
        tabs.addTab("Names", names = new NameView(addr -> {
            data.setAddress(addr);
            tabs.setSelectedIndex(0);
        }));
        tabs.addTab("Types", types = new DatatypeView(trc.getTypeDatabase(), status::setText));
        tabs.addTab("Dynamic", dynamic = new DynamicDataView(trc, status::setText));
        add(BorderLayout.CENTER, tabs);
        add(BorderLayout.SOUTH, status);

        if (trc != null) {
            setTraceAnalyzer(trc);
        }

        setSize(800, 600);
        setLocationRelativeTo(null);

        step.addStepListener(data);
        step.addStepListener(segments);
        step.addStepListener(dynamic);

        TraceListener listener = this::setTraceAnalyzer;
        trace.addTraceListener(listener);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                step.removeStepListener(data);
                step.removeStepListener(segments);
                step.removeStepListener(dynamic);
                trace.removeTraceListener(listener);
                dispose();
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        data.setTraceAnalyzer(trc);
        types.setTypeDatabase(trc.getTypeDatabase());
        segments.setTraceAnalyzer(trc);
        names.setTraceAnalyzer(trc);
        dynamic.setTraceAnalyzer(trc);
    }
}
