package org.graalvm.vm.trcview.ui;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.util.ui.MessageBox;

@SuppressWarnings("serial")
public class ImportMapFileDialog extends JDialog {
    private static final Logger log = Trace.create(ImportMapFileDialog.class);

    private int base = 16;

    private JFormattedTextField baseAddress;
    private String filename;

    private MainWindow main;

    public ImportMapFileDialog(MainWindow owner) {
        super(owner, "Import map file...", false);

        this.main = owner;

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        setLayout(new BorderLayout());

        HexFormat fmt = new HexFormat();

        JButton ok = new JButton("ok");

        JPanel center = new JPanel(new LabeledPairLayout());
        center.add(LabeledPairLayout.LABEL, new JLabel("Base address:"));
        center.add(LabeledPairLayout.COMPONENT, baseAddress = new JFormattedTextField(fmt));

        FileDialog save = new FileDialog(this, "Open...", FileDialog.LOAD);
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
        center.add(LabeledPairLayout.LABEL, new JLabel("Filename:"));
        center.add(LabeledPairLayout.COMPONENT, fileinput);

        baseAddress.setValue((long) 0);

        baseAddress.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(baseAddress::selectAll);
            }

            @Override
            public void focusLost(FocusEvent e) {
                // ignore
            }
        });

        add(BorderLayout.CENTER, center);

        JPanel south = new JPanel(new FlowLayout());
        JButton cancel = new JButton("Cancel");

        ok.addActionListener(e -> {
            setVisible(false);
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        load();
                    } catch (IOException ex) {
                        log.log(Levels.WARNING, "I/O exception while writing file: " + ex.getMessage(), ex);
                        MessageBox.showError(ImportMapFileDialog.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
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
    }

    private class HexFormat extends JFormattedTextField.AbstractFormatter {
        @Override
        public Object stringToValue(String text) throws ParseException {
            try {
                return Long.parseUnsignedLong(text, base);
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
                    return Long.toUnsignedString((long) value, base);
                } catch (Throwable t) {
                    throw new ParseException(t.getMessage(), 0);
                }
            }
        }
    }

    public void focus() {
        baseAddress.requestFocusInWindow();
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        if (trc != null) {
            base = trc.getArchitecture().getFormat().numberfmt == StepFormat.NUMBERFMT_OCT ? 8 : 16;
        }
    }

    private void load() throws IOException {
        long reloc = (long) baseAddress.getValue();
        main.loadMap(new File(filename), reloc);
    }
}
