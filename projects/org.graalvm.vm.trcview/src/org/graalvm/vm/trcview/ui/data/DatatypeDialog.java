package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.graalvm.vm.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class DatatypeDialog extends JDialog {
    private DatatypeView types;

    public DatatypeDialog(JFrame owner, TraceAnalyzer trc) {
        super(owner, "Data Types", false);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, types = new DatatypeView(trc.getTypeDatabase()));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        if (trc != null) {
            setTraceAnalyzer(trc);
        }

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        types.setTypeDatabase(trc.getTypeDatabase());
    }
}
