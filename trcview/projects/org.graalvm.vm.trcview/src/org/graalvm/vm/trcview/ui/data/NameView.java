package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;

import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.AbstractDocumentFilter;
import org.graalvm.vm.trcview.ui.MainWindow;

@SuppressWarnings("serial")
public class NameView extends JPanel {
    private TraceAnalyzer trc;
    private final Consumer<Long> jump;

    private List<Name> data;
    private List<Name> filtered;
    private JList<Name> names;
    private Model model;

    private JTextField filter;
    private String filterString;

    private boolean showUnnamed = true;

    public NameView(Consumer<Long> jump) {
        super(new BorderLayout());
        this.jump = jump;

        data = new ArrayList<>();
        filtered = data;
        names = new JList<>(model = new Model());
        names.setFont(MainWindow.FONT);

        names.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    jump();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        names.getInputMap().put(enter, enter);
        names.getActionMap().put(enter, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                jump();
            }
        });

        filter = new JTextField();
        int sz = filter.getFont().getSize();
        filter.setFont(new Font(Font.MONOSPACED, Font.PLAIN, sz));
        ((AbstractDocument) filter.getDocument()).setDocumentFilter(new AbstractDocumentFilter() {
            @Override
            public boolean test(String s) {
                if (s.contains(" ")) {
                    return false;
                }
                return true;
            }
        });
        filter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateFilter();
            }
        });

        JButton clear = new JButton("x");
        clear.addActionListener(e -> {
            filter.setText("");
            updateFilter();
        });

        JCheckBox unnamedvars = new JCheckBox();
        unnamedvars.addItemListener(e -> {
            showUnnamed = unnamedvars.isSelected();
            update();
        });

        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        optionsPanel.add(new JLabel("Show unnamed variables:"));
        optionsPanel.add(unnamedvars);

        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.add(BorderLayout.CENTER, filter);
        filterPanel.add(BorderLayout.EAST, clear);

        add(BorderLayout.NORTH, optionsPanel);
        add(BorderLayout.CENTER, new JScrollPane(names));
        add(BorderLayout.SOUTH, filterPanel);
    }

    private void jump() {
        Name name = names.getSelectedValue();
        if (name != null) {
            jump.accept(name.getAddress());
        }
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        update();
    }

    public void nameChanged() {
        update();
    }

    private static boolean isSyntheticName(String name) {
        if (name.startsWith("sub_") || name.startsWith("loc_")) {
            return true;
        } else {
            return false;
        }
    }

    private void update() {
        filterString = null;
        data.clear();
        Stream<Variable> stream = trc.getTypedMemory().getAllTypes().stream();
        if (!showUnnamed) {
            stream = stream.filter(x -> x.getRawName() != null);
        }
        StepFormat fmt = trc.getArchitecture().getFormat();
        List<Name> variables = stream.map(x -> new Name(x.getName(fmt), x.getAddress())).collect(Collectors.toList());
        variables.addAll(trc.getSymbols().stream().filter(x -> !isSyntheticName(x.name)).map(x -> new Name(x.name, x.address)).collect(Collectors.toList()));
        variables.stream().sorted().forEach(data::add);
        updateFilter();
    }

    private void updateFilter() {
        String match = filter.getText().trim();
        if (filterString != null && filterString.equals(match)) {
            // no change
            return;
        } else {
            filterString = match;
        }

        if (match.length() == 0) {
            filtered = data;
        } else {
            filtered = data.stream().filter(s -> s.name.contains(match)).collect(Collectors.toList());
        }
        model.update();
    }

    private static class Name implements Comparable<Name> {
        private String name;
        private long address;

        public Name(String name, long address) {
            this.name = name;
            this.address = address;
        }

        public long getAddress() {
            return address;
        }

        @Override
        public String toString() {
            return name;
        }

        public int compareTo(Name o) {
            return name.compareTo(o.name);
        }
    }

    private class Model extends AbstractListModel<Name> {
        public Name getElementAt(int index) {
            return filtered.get(index);
        }

        public int getSize() {
            return filtered.size();
        }

        public void update() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
