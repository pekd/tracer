package org.graalvm.vm.trcview.ui.data;

import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;

public class DataElement extends DefaultElement {
    private final long location;
    private final boolean hasLocation;

    public DataElement(String text, int type) {
        super(text, type);
        location = 0;
        hasLocation = false;
    }

    public DataElement(String text, int type, long location) {
        super(text, type);
        this.location = location;
        hasLocation = true;
    }

    public boolean hasLocation() {
        return hasLocation;
    }

    public long getLocation() {
        return location;
    }
}
