package org.graalvm.vm.trcview.ui.data.struct;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

@SuppressWarnings("serial")
public class StructEditor extends JPanel {
    private JTextPane editor;
    private StructDocument structs;

    public StructEditor() {
        super(new BorderLayout());
        editor = new JTextPane(structs = new StructDocument());
        editor.setCaretPosition(structs.getInitialCaret());
        add(BorderLayout.CENTER, new JScrollPane(editor));
    }
}
