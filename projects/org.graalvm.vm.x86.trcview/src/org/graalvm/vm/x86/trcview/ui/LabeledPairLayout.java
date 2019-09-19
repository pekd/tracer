package org.graalvm.vm.x86.trcview.ui;

import static java.lang.Math.max;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LabeledPairLayout implements LayoutManager {
    public static final String LABEL = "label";
    public static final String COMPONENT = "component";

    private List<Component> labels = new ArrayList<>();
    private List<Component> components = new ArrayList<>();

    private int gapX = 2;
    private int gapY = 2;

    private int insetX = 5;
    private int insetY = 5;

    @Override
    public void addLayoutComponent(String s, Component c) {
        if (s.equals(LABEL)) {
            labels.add(c);
        } else {
            components.add(c);
        }
    }

    @Override
    public void layoutContainer(Container c) {
        Insets insets = c.getInsets();

        int labelWidth = 0;
        for (Component label : labels) {
            labelWidth = max(labelWidth, label.getPreferredSize().width);
        }

        int y = insets.top + insetY;

        Iterator<Component> componentit = components.iterator();
        Iterator<Component> labelit = labels.iterator();
        while (labelit.hasNext() && componentit.hasNext()) {
            Component label = labelit.next();
            Component component = componentit.next();
            int height = max(label.getPreferredSize().height, component.getPreferredSize().height);
            label.setBounds(insets.left + insetX, y, labelWidth, height);
            component.setBounds(insets.left + insetX + labelWidth + gapX, y, c.getSize().width - (labelWidth + gapX + insets.left + 2 * insetX + insets.right), height);
            y += (height + gapY);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container c) {
        Insets insets = c.getInsets();

        int labelWidth = 0;
        for (Component label : labels) {
            labelWidth = max(labelWidth, label.getPreferredSize().width);
        }

        int y = insets.top + insetY;

        Iterator<Component> labelit = labels.iterator();
        Iterator<Component> componentit = components.iterator();
        while (labelit.hasNext() && componentit.hasNext()) {
            Component label = labelit.next();
            Component component = componentit.next();
            int height = max(label.getPreferredSize().height, component.getPreferredSize().height);
            y += (height + gapY);
        }

        return new Dimension(labelWidth * 3 + 2 * insetX, y + insetY);
    }

    @Override
    public Dimension preferredLayoutSize(Container c) {
        Dimension d = minimumLayoutSize(c);
        d.width *= 2;
        return d;
    }

    @Override
    public void removeLayoutComponent(Component c) {
        int index = components.indexOf(c);
        if (index != -1) {
            labels.remove(index);
            components.remove(index);
        }
    }
}
