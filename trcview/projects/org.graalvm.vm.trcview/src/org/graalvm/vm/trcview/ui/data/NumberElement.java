package org.graalvm.vm.trcview.ui.data;

public class NumberElement extends DataElement {
    private final long value;

    public NumberElement(String text, int type, long value) {
        super(text, type);
        this.value = value;
    }

    public NumberElement(String text, int type, long self, long value) {
        super(text, type, self);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
