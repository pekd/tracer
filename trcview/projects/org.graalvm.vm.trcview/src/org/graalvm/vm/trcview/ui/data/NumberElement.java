package org.graalvm.vm.trcview.ui.data;

import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;

public class NumberElement extends DefaultElement {
    private final long value;

    public NumberElement(String text, int type, long value) {
        super(text, type);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
