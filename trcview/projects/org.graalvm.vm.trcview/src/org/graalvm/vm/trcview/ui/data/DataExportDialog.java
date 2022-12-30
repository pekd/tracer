package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class DataExportDialog extends JDialog {
    private final TraceAnalyzer trc;
    private final long step;

    private final JTextPane text;

    public DataExportDialog(JFrame owner, TraceAnalyzer trc, Variable var, long step) {
        super(owner, "Export data...", true);
        this.trc = trc;
        this.step = step;

        setLayout(new BorderLayout());

        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/plain");

        add(BorderLayout.CENTER, new JScrollPane(text));

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        text.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        text.getActionMap().put(esc, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        StepFormat fmt = trc.getArchitecture().getFormat();
        if (var.getType() == null) {
            // no type available
            text.setText("// no type available for " + var.getName(fmt));
        } else {
            String data = loadData(var.getType(), var.getAddress());
            String prototype = var.getType().toCType(var.getName(fmt)) + " = ";
            text.setText(prototype + data + ";");
        }

        setSize(640, 480);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private String loadData(org.graalvm.vm.trcview.analysis.type.Type type, long address) {
        switch (type.getType()) {
            case CODE: {
                try {
                    org.graalvm.vm.trcview.analysis.type.Type u8 = new org.graalvm.vm.trcview.analysis.type.Type(DataType.U8);
                    List<String> elements = new ArrayList<>();
                    for (int i = 0; i < type.getSize(); i++) {
                        elements.add(DecoderUtils.str(u8, Byte.toUnsignedInt(trc.getI8(address + i, step)), step, trc, false));
                    }
                    return "{ " + String.join(", ", elements) + " }";
                } catch (MemoryNotMappedException e) {
                    return "???";
                }
            }
            case STRUCT:
                return "???";
            default:
                try {
                    if (type.getElements() > 1) {
                        List<String> elements = new ArrayList<>();
                        long size = type.getElementSize();
                        for (int i = 0; i < type.getElements(); i++) {
                            elements.add(DecoderUtils.str(type.getElementType(), read(address + i * size, size), step, trc, false));
                        }
                        return "{ " + String.join(", ", elements) + " }";
                    } else {
                        return DecoderUtils.str(type, read(address, type.getSize()), step, trc, false);
                    }
                } catch (MemoryNotMappedException e) {
                    return "???";
                }
        }
    }

    private long read(long addr, long size) throws MemoryNotMappedException {
        switch ((int) size) {
            default:
            case 1:
                return Byte.toUnsignedInt(trc.getI8(addr, step));
            case 2:
                return Short.toUnsignedInt(trc.getI16(addr, step));
            case 4:
                return Integer.toUnsignedLong(trc.getI32(addr, step));
            case 8:
                return trc.getI64(addr, step);
        }
    }
}
