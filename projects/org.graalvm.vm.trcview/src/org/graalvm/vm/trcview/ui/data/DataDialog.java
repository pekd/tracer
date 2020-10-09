package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.StepListenable;

@SuppressWarnings("serial")
public class DataDialog extends JDialog {
    private DataView data;
    private DatatypeView types;

    public DataDialog(JFrame owner, TraceAnalyzer trc, StepListenable step) {
        super(owner, "Data", false);

        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Data", data = new DataView());
        tabs.addTab("Types", types = new DatatypeView(trc.getTypeDatabase(), s -> {
        }));
        add(BorderLayout.CENTER, tabs);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        tabs.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        tabs.getActionMap().put(esc, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step.removeStepListener(data);
                dispose();
            }
        });

        if (trc != null) {
            setTraceAnalyzer(trc);
        }

        setSize(800, 600);
        setLocationRelativeTo(null);

        step.addStepListener(data);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                step.removeStepListener(data);
                dispose();
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        data.setTraceAnalyzer(trc);
        types.setTypeDatabase(trc.getTypeDatabase());
    }
}
