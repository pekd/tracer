package org.graalvm.vm.trcview.ui.device;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceRegister;
import org.graalvm.vm.trcview.analysis.device.FieldFormat;
import org.graalvm.vm.trcview.analysis.device.RegisterAccess;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.ui.Utils;

@SuppressWarnings("serial")
public class DeviceRegisterView extends JPanel {
    private static final String STYLE = "<style>\n" +
                    "html, body, pre {\n" +
                    "    padding: 0;\n" +
                    "    margin: 0;\n" +
                    "}\n" +
                    "pre {\n" +
                    "    font-family: " + Font.MONOSPACED + ";\n" +
                    "    font-size: 11pt;\n" +
                    "}\n" +
                    ".unknown {\n" +
                    "    color: gray;\n" +
                    "}\n" +
                    ".comment {\n" +
                    "    color: gray;\n" +
                    "}\n" +
                    ".change {\n" +
                    "    color: red;\n" +
                    "}\n" +
                    "</style>";

    private StepEvent step;

    private List<DeviceRegister> regs = Collections.emptyList();
    private JPanel noregs;

    private JSplitPane split;

    private JTextPane text;

    private JList<DeviceRegister> reglist;
    private RegisterListModel model;

    public DeviceRegisterView() {
        super(new BorderLayout());

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        text = new JTextPane();
        text.setEditorKit(new HTMLEditorKit());
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/html");
        split.setRightComponent(new JScrollPane(text));

        noregs = new JPanel(new FlowLayout(FlowLayout.CENTER));
        noregs.add(new JLabel("This device has no registers"));

        reglist = new JList<>(model = new RegisterListModel());
        reglist.addListSelectionListener(e -> update());
        split.setLeftComponent(new JScrollPane(reglist));

        add(BorderLayout.CENTER, noregs);

        split.setResizeWeight(0.2);
    }

    public void setDevice(Device dev) {
        regs = dev.getRegisters().values().stream().sorted((a, b) -> a.getId() - b.getId()).collect(Collectors.toList());
        removeAll();
        reglist.clearSelection();
        if (regs.isEmpty()) {
            add(BorderLayout.CENTER, noregs);
        } else {
            add(BorderLayout.CENTER, split);
            model.changed();
        }
        revalidate();
        repaint();
        update();
    }

    public void setStep(StepEvent step) {
        this.step = step;
        update();
    }

    private void update() {
        if (step == null) {
            return;
        }

        List<DeviceRegister> selected = reglist.getSelectedValuesList();
        if (selected.isEmpty()) {
            selected = Collections.unmodifiableList(regs);
        }

        StepFormat fmt = step.getFormat();
        StringBuilder buf = new StringBuilder();
        buf.append("<html>");
        buf.append(STYLE);
        buf.append("<body><pre>");
        int count = selected.size();
        int i = 0;
        for (DeviceRegister reg : selected) {
            i++;
            RegisterAccess value = reg.getLastValue(step.getStep());
            RegisterAccess read = reg.getLastRead(step.getStep());
            RegisterAccess write = reg.getLastWrite(step.getStep());

            buf.append(Utils.html(reg.getName()));
            buf.append(" = ");
            if (value == null) {
                buf.append("<i class=\"unknown\">unknown</i>");
            } else {
                buf.append(fmt.formatWord(value.value));
            }

            if (read != null || write != null) {
                buf.append(" <span class=\"comment\">[");
                if (read != null) {
                    buf.append("last read: ");
                    buf.append(read.step);
                    if (write != null) {
                        buf.append(", ");
                    }
                }
                if (write != null) {
                    buf.append("last write: ");
                    buf.append(write.step);
                }
                buf.append("]</span>");
            }

            if (value != null) {
                List<FieldFormat> format = reg.getFormat();
                if (!format.isEmpty()) {
                    buf.append("\n");
                }
                for (FieldFormat f : format) {
                    buf.append("    ");
                    buf.append(Utils.html(f.getName()));
                    buf.append(" = ");
                    buf.append(f.format(value.value));
                    buf.append("\n");
                }
            }

            if (i != count) {
                buf.append("\n");
            }
        }
        buf.append("</pre></body></html>");

        text.setText(buf.toString());
        text.setCaretPosition(0);
    }

    private class RegisterListModel extends AbstractListModel<DeviceRegister> {
        public int getSize() {
            return regs.size();
        }

        public DeviceRegister getElementAt(int index) {
            return regs.get(index);
        }

        public void changed() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
