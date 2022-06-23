package org.graalvm.vm.trcview.ui.call;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class ABIEditor extends JDialog {
    private CallEditor call;
    private SyscallEditor syscall;
    private SyscallCallingConventionEditor syscallArgs;

    public ABIEditor(JFrame owner, TraceAnalyzer trc) {
        super(owner, "ABI Editor", false);

        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Subroutines", call = new CallEditor());
        tabs.add("System Calls", syscallArgs = new SyscallCallingConventionEditor());
        tabs.add("System Call List", syscall = new SyscallEditor());
        add(BorderLayout.CENTER, tabs);

        JPanel south = new JPanel(new FlowLayout());
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> {
            if (call.check() && syscallArgs.check() && syscall.check()) {
                call.commit();
                syscallArgs.commit();
                syscall.commit();
                dispose();
            }
        });
        cancel.addActionListener(e -> dispose());

        south.add(ok);
        south.add(cancel);
        add(BorderLayout.SOUTH, south);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        tabs.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        tabs.getActionMap().put(esc, new AbstractAction() {
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
        call.setTraceAnalyzer(trc);
        syscall.setTraceAnalyzer(trc);
        syscallArgs.setTraceAnalyzer(trc);
    }
}
