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

@SuppressWarnings("serial")
public class DataDialog extends JDialog {
    private DataView data;
    private DatatypeView types;
    private MemorySegmentView segments;

    public DataDialog(JFrame owner, TraceAnalyzer trc, StepListenable step) {
        super(owner, "Data", false);

        setLayout(new BorderLayout());

        JLabel status = new JLabel("Ready");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Data", data = new DataView());
        tabs.addTab("Segments", segments = new MemorySegmentView(addr -> {
            data.setAddress(addr);
            tabs.setSelectedIndex(0);
        }));
        tabs.addTab("Types", types = new DatatypeView(trc.getTypeDatabase(), status::setText));
        add(BorderLayout.CENTER, tabs);
        add(BorderLayout.SOUTH, status);

        if (trc != null) {
            setTraceAnalyzer(trc);
        }

        setSize(800, 600);
        setLocationRelativeTo(null);

        step.addStepListener(data);
        step.addStepListener(segments);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                step.removeStepListener(data);
                step.removeStepListener(segments);
                dispose();
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        data.setTraceAnalyzer(trc);
        types.setTypeDatabase(trc.getTypeDatabase());
        segments.setTraceAnalyzer(trc);
    }
}
