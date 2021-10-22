package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.graalvm.vm.trcview.ui.data.struct.StructEditor;

@SuppressWarnings("serial")
public class StructEditorDialog extends JDialog {
    public StructEditorDialog(JFrame owner) {
        super(owner, "Struct Editor", false);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new StructEditor());

        setSize(300, 300);
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // step.removeStepListener(data);
                dispose();
            }
        });
    }
}
