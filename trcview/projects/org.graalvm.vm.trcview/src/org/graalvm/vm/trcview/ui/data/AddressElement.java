package org.graalvm.vm.trcview.ui.data;

public class AddressElement extends DataElement {
    private final long address;

    public AddressElement(String text, int type, long address) {
        super(text, type);
        this.address = address;
    }

    public AddressElement(String text, int type, long self, long address) {
        super(text, type, self);
        this.address = address;
    }

    public long getAddress() {
        return address;
    }
}
