package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class DatatypeDialog extends JDialog {
    private DatatypeView types;

    public DatatypeDialog(JFrame owner, TraceAnalyzer trc) {
        super(owner, "Data Types", false);

        JLabel status = new JLabel("Ready");

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, types = new DatatypeView(trc.getTypeDatabase(), status::setText));
        add(BorderLayout.SOUTH, status);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        types.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        types.getActionMap().put(esc, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

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
