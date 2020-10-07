package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.analysis.type.NameAlreadyUsedException;
import org.graalvm.vm.trcview.analysis.type.NameValidator;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.TypeAlias;
import org.graalvm.vm.trcview.analysis.type.UserDefinedType;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.util.HexFormatter;

@SuppressWarnings("serial")
public class DatatypeView extends JPanel {
    private JList<UserDefinedType> types;
    private JTextArea text;
    private Model model;

    private UserTypeDatabase typeDatabase;

    public DatatypeView(UserTypeDatabase db, Consumer<String> status) {
        super(new BorderLayout());

        model = new Model();
        types = new JList<>(model);
        types.setCellRenderer(new Renderer());
        text = new JTextArea();
        text.setFont(MainWindow.FONT);
        text.setForeground(Color.BLACK);
        text.setEnabled(false);
        JPanel left = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout());
        JButton add = new JButton("+");
        JButton rename = new JButton("R");
        JButton remove = new JButton("-");
        buttons.add(add);
        buttons.add(rename);
        buttons.add(remove);
        left.add(BorderLayout.CENTER, new JScrollPane(types));
        left.add(BorderLayout.SOUTH, buttons);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(left);
        split.setRightComponent(new JScrollPane(text));
        add(BorderLayout.CENTER, split);

        add.addActionListener(e -> create());
        rename.addActionListener(e -> rename());
        remove.addActionListener(e -> remove());

        KeyStroke n = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0);
        left.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(n, n);
        left.getActionMap().put(n, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename();
            }
        });

        KeyStroke ins = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0);
        left.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ins, ins);
        left.getActionMap().put(ins, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                create();
            }
        });

        KeyStroke del = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        left.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(del, del);
        left.getActionMap().put(del, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove();
            }
        });

        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(f9, f9);
        getActionMap().put(f9, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTextView();
            }
        });

        types.addListSelectionListener(e -> {
            updateTextView();
        });

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String def = text.getText().trim();
                if (def.length() > 0) {
                    try {
                        Struct struct = new Parser(def, typeDatabase).parseStruct(false);
                        text.setForeground(Color.BLACK);
                        UserDefinedType type = types.getSelectedValue();
                        if (type == null || !(type instanceof Struct)) {
                            return;
                        }
                        Struct s = (Struct) type;
                        s.set(struct);
                        types.repaint();
                        status.accept("Struct parsed successfully");
                    } catch (ParseException ex) {
                        text.setForeground(Color.RED);
                        status.accept("Parse error: " + ex.getMessage());
                    }
                } else {
                    text.setForeground(Color.BLACK);
                    status.accept("Ready");
                }
            }
        });

        setTypeDatabase(db);
    }

    private void create() {
        String name = JOptionPane.showInputDialog(this, "Enter type name:", "New type...", JOptionPane.PLAIN_MESSAGE);
        if (name != null && name.trim().length() > 0) {
            name = name.trim();
            if (!NameValidator.isValidName(name)) {
                JOptionPane.showMessageDialog(this, "Invalid name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                typeDatabase.add(new Struct(name));
                update();
            } catch (NameAlreadyUsedException ex) {
                JOptionPane.showMessageDialog(this, "A type with name " + name + " exists already", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rename() {
        UserDefinedType type = types.getSelectedValue();
        if (type == null) {
            return;
        }
        String name = (String) JOptionPane.showInputDialog(this, "Enter type name:", "New type...", JOptionPane.PLAIN_MESSAGE, null, null, type.getName());
        if (name != null && name.trim().length() > 0) {
            name = name.trim();
            if (!NameValidator.isValidName(name)) {
                JOptionPane.showMessageDialog(this, "Invalid name", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                typeDatabase.rename(type, name);
                update();
            } catch (NameAlreadyUsedException ex) {
                JOptionPane.showMessageDialog(this, "A type with name " + name + " exists already", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void remove() {
        UserDefinedType type = types.getSelectedValue();
        if (type != null) {
            typeDatabase.undefine(type.getName());
            update();
        }
    }

    private void updateTextView() {
        UserDefinedType type = types.getSelectedValue();
        if (type == null) {
            text.setText("");
            text.setEnabled(false);
        } else if (type instanceof Struct) {
            text.setText(((Struct) type).prettyprint() + ";");
            text.setEnabled(true);
            text.setForeground(Color.BLACK);
        } else {
            text.setText("/* unsupported type */");
            text.setEnabled(false);
        }
    }

    private void update() {
        int idx = types.getSelectedIndex();
        List<UserDefinedType> data = new ArrayList<>(typeDatabase.getTypes());
        Collections.sort(data, (a, b) -> a.getName().compareTo(b.getName()));
        model.set(data);
        if (idx != -1) {
            if (idx < data.size()) {
                types.setSelectedIndex(idx);
            } else if (!data.isEmpty()) {
                types.setSelectedIndex(data.size() - 1);
            } else {
                types.clearSelection();
            }
        } else {
            types.clearSelection();
        }
        updateTextView();
    }

    public void setTypeDatabase(UserTypeDatabase db) {
        typeDatabase = db;
        update();
    }

    private static class Model extends AbstractListModel<UserDefinedType> {
        private List<UserDefinedType> data = Collections.emptyList();

        public UserDefinedType getElementAt(int index) {
            return data.get(index);
        }

        public int getSize() {
            return data.size();
        }

        public void set(List<UserDefinedType> data) {
            this.data = data;
            fireContentsChanged(this, 0, getSize());
        }
    }

    private static class Renderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String str;
            UserDefinedType type = (UserDefinedType) value;
            if (type instanceof Struct) {
                Struct struct = (Struct) type;
                str = "struct " + type.getName() + " { /* size=0x" + HexFormatter.tohex(struct.getSize()) + " */ }";
            } else if (type instanceof TypeAlias) {
                TypeAlias alias = (TypeAlias) type;
                str = "typedef " + alias.getType() + " " + type.getName();
            } else {
                str = "/* unknown: " + type.getName() + " */";
            }
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ((JLabel) c).setText(str);
            return c;
        }
    }
}
