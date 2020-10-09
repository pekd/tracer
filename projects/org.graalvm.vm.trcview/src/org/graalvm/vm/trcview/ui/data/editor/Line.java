package org.graalvm.vm.trcview.ui.data.editor;

import java.util.List;

public abstract class Line {
    public abstract List<Element> getElements();

    public int getLength() {
        int sum = 0;
        for (Element element : getElements()) {
            sum += element.getLength();
        }
        return sum;
    }
}
