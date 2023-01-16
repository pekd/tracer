package org.graalvm.vm.trcview.ui.data;

import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;

public class LineAddressElement extends DefaultElement {
    private final long address;

    public LineAddressElement(String text, int type, long address) {
        super(text, type);
        this.address = address;
    }

    public long getAddress() {
        return address;
    }
}
