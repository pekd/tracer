package org.graalvm.vm.x86.trcview.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.VirtualMemorySnapshot;

@SuppressWarnings("serial")
public class ExportMemoryDialog extends JDialog {
    private static final Logger log = Trace.create(ExportMemoryDialog.class);

    private JFormattedTextField startAddress;
    private JFormattedTextField endAddress;
    private JFormattedTextField size;
    private JCheckBox ignoreUnmapped;
    private JTextPane preview;
    private String filename;

    private VirtualMemorySnapshot memory;

    public ExportMemoryDialog(Frame owner) {
        super(owner, "Export memory...", false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setLayout(new BorderLayout());

        HexFormat fmt = new HexFormat();

        JButton ok = new JButton("ok");

        JPanel addresses = new JPanel(new LabeledPairLayout());
        addresses.add(LabeledPairLayout.LABEL, new JLabel("Start address:"));
        addresses.add(LabeledPairLayout.COMPONENT, startAddress = new JFormattedTextField(fmt));
        addresses.add(LabeledPairLayout.LABEL, new JLabel("End address:"));
        addresses.add(LabeledPairLayout.COMPONENT, endAddress = new JFormattedTextField(fmt));
        addresses.add(LabeledPairLayout.LABEL, new JLabel("Size:"));
        addresses.add(LabeledPairLayout.COMPONENT, size = new JFormattedTextField(fmt));

        FileDialog save = new FileDialog(this, "Open...", FileDialog.SAVE);
        JPanel fileinput = new JPanel(new BorderLayout());
        JTextField filenameView = new JTextField();
        filenameView.setEditable(false);
        JButton selectfile = new JButton("...");
        selectfile.addActionListener(e -> {
            save.setVisible(true);
            if (save.getFile() != null) {
                filename = save.getDirectory() + save.getFile();
                filenameView.setText(filename);
                ok.setEnabled(true);
            }
        });
        fileinput.add(BorderLayout.CENTER, filenameView);
        fileinput.add(BorderLayout.EAST, selectfile);
        addresses.add(LabeledPairLayout.LABEL, new JLabel("Filename:"));
        addresses.add(LabeledPairLayout.COMPONENT, fileinput);

        JPanel ignoreUnmappedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ignoreUnmappedPanel.add(new JLabel("Ignore unmapped memory:"));
        ignoreUnmappedPanel.add(ignoreUnmapped = new JCheckBox());
        ignoreUnmappedPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        ignoreUnmappedPanel.setToolTipText("Replace unmapped memory regions by 0x00 bytes in the exported file");

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.PAGE_AXIS));
        options.add(addresses);
        options.add(ignoreUnmappedPanel);

        JPanel center = new JPanel(new BorderLayout());
        center.add(BorderLayout.NORTH, options);
        center.add(BorderLayout.CENTER, preview = new JTextPane());

        preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        preview.setEditable(false);
        preview.setContentType("text/plain");

        startAddress.setValue((long) 0);
        endAddress.setValue((long) 0);
        size.setValue((long) 0);

        ignoreUnmapped.setSelected(true);

        startAddress.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(startAddress::selectAll);
            }

            @Override
            public void focusLost(FocusEvent e) {
                // ignore
            }
        });

        endAddress.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(endAddress::selectAll);
            }

            @Override
            public void focusLost(FocusEvent e) {
                // ignore
            }
        });

        size.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(size::selectAll);
            }

            @Override
            public void focusLost(FocusEvent e) {
                // ignore
            }
        });

        startAddress.addPropertyChangeListener("value", e -> {
            long start = (long) startAddress.getValue();
            long end = (long) endAddress.getValue();

            if (end < start) {
                end = start;
                endAddress.setValue(end);
            }

            long bytes = end - start;
            size.setText(Long.toUnsignedString(bytes, 16));

            update();
        });

        endAddress.addPropertyChangeListener("value", e -> {
            long start = (long) startAddress.getValue();
            long end = (long) endAddress.getValue();

            if (end < start) {
                start = end;
                startAddress.setValue(start);
            }

            long bytes = end - start;
            size.setText(Long.toUnsignedString(bytes, 16));

            update();
        });

        size.addPropertyChangeListener("value", e -> {
            long start = (long) startAddress.getValue();
            long bytes = (long) size.getValue();
            long end = start + bytes;
            endAddress.setValue(end);

            update();
        });

        add(BorderLayout.CENTER, center);

        JPanel south = new JPanel(new FlowLayout());
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(e -> {
            setVisible(false);
            try {
                write();
            } catch (IOException ex) {
                log.log(Levels.WARNING, "I/O exception while writing file: " + ex.getMessage(), ex);
            }
        });

        cancel.addActionListener(e -> {
            setVisible(false);
        });

        ok.setEnabled(filename != null);

        south.add(ok);
        south.add(cancel);

        add(BorderLayout.SOUTH, south);

        getRootPane().setDefaultButton(ok);

        setSize(400, 300);
        setLocationRelativeTo(null);

        update();
    }

    private static class HexFormat extends JFormattedTextField.AbstractFormatter {
        @Override
        public Object stringToValue(String text) throws ParseException {
            try {
                return Long.parseUnsignedLong(text, 16);
            } catch (NumberFormatException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value == null) {
                return "0";
            } else {
                try {
                    return Long.toUnsignedString((long) value, 16);
                } catch (Throwable t) {
                    throw new ParseException(t.getMessage(), 0);
                }
            }
        }
    }

    public void focus() {
        startAddress.requestFocusInWindow();
    }

    public void setMemory(VirtualMemorySnapshot memory) {
        this.memory = memory;
        update();
    }

    private String dump(long addr, long count) {
        StringBuilder buf = new StringBuilder();
        String tail = "";
        int cnt = (int) count;
        if (cnt < 0) {
            return "";
        } else if (cnt > 256) {
            cnt = 256;
            tail = "...";
        }

        for (int i = 0; i < cnt; i++) {
            try {
                byte b = memory.getI8(addr + i);
                buf.append(HexFormatter.tohex(Byte.toUnsignedInt(b), 2));
                buf.append(' ');
            } catch (MemoryNotMappedException e) {
                buf.append("-- ");
            }
        }

        return buf.append(tail).toString().trim();
    }

    private void update() {
        long addr = (long) startAddress.getValue();
        long cnt = (long) size.getValue();
        String text = dump(addr, cnt);
        preview.setText(text);
    }

    private void write() throws IOException {
        long start = (long) startAddress.getValue();
        long end = (long) endAddress.getValue();
        boolean ignore = ignoreUnmapped.isSelected();
        log.info(String.format("Dumping memory from 0x%x to 0x%x to file %s", start, end, filename));
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
            for (long ptr = start; ptr < end; ptr++) {
                byte b = 0;
                try {
                    b = memory.getI8(ptr);
                } catch (MemoryNotMappedException e) {
                    if (!ignore) {
                        log.warning("Memory dump failed due to unmapped memory: " + e.getMessage());
                        JOptionPane.showMessageDialog(this, e.getMessage(), "Memory not mapped", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                out.write(b);
            }
        }
        log.info("Memory dump complete");
    }
}
