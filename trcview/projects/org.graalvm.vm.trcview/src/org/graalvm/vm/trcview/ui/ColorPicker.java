package org.graalvm.vm.trcview.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ColorPicker extends JDialog {
    public static final Color[] DEFAULT_COLORS = {
                    new Color(233, 64, 62),
                    new Color(232, 132, 63),
                    new Color(233, 195, 67),
                    new Color(218, 236, 86),
                    new Color(152, 233, 62),
                    new Color(90, 222, 68),
                    new Color(61, 226, 106),
                    new Color(58, 222, 164),
                    new Color(55, 210, 209),
                    new Color(73, 178, 237),
                    new Color(106, 140, 239),
                    new Color(131, 117, 241),
                    new Color(170, 96, 238),
                    new Color(212, 77, 234),
                    new Color(233, 63, 194),
                    new Color(233, 63, 135),
    };

    private static final int COLORS_PER_ROW = 8;

    public int getColorCount() {
        return DEFAULT_COLORS.length;
    }

    public Color getColor(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("invalid color " + i);
        }
        return DEFAULT_COLORS[i];
    }

    private int color = -1;

    public ColorPicker(Frame owner) {
        super(owner, "Choose a color...", true);

        setLayout(new BorderLayout());
        int rows = (int) Math.ceil(getColorCount() / (double) COLORS_PER_ROW);
        JPanel colors = new JPanel(new GridLayout(rows, COLORS_PER_ROW));

        for (int i = 0; i < getColorCount(); i++) {
            final int c = i;
            JButton button = new JButton();
            button.setBackground(getColor(i));
            button.setSelected(false);
            button.addActionListener(e -> {
                setColor(c);
                dispose();
            });
            colors.add(button);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton cancel = new JButton("Clear");
        cancel.addActionListener(e -> dispose());
        buttons.add(cancel);

        add(BorderLayout.CENTER, colors);
        add(BorderLayout.SOUTH, buttons);

        setSize(300, 150);
        setLocationRelativeTo(null);
    }

    private void setColor(int i) {
        color = i;
    }

    public Color getColor() {
        if (color == -1) {
            return null;
        } else {
            return getColor(color);
        }
    }
}
