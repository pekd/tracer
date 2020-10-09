package org.graalvm.vm.trcview.ui.data.editor;

import java.util.ArrayList;
import java.util.List;

public class DefaultLine extends Line {
    private List<Element> elements = new ArrayList<>();

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
