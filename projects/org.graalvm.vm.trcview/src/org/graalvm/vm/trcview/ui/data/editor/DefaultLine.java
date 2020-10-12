package org.graalvm.vm.trcview.ui.data.editor;

import java.util.ArrayList;
import java.util.List;

public class DefaultLine extends Line {
    private List<Element> elements;

    public DefaultLine() {
        elements = new ArrayList<>();
    }

    public DefaultLine(Element... initial) {
        this();
        for (Element e : initial) {
            elements.add(e);
        }
    }

    public void addElement(Element element) {
        elements.add(element);
    }

    public void removeElement(int element) {
        elements.remove(element);
    }

    @Override
    public List<Element> getElements() {
        return elements;
    }
}
