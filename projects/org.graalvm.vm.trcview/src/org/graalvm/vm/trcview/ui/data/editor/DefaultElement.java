package org.graalvm.vm.trcview.ui.data.editor;

public class DefaultElement extends Element {
    private final String text;

    public DefaultElement(String text, int type) {
        this.text = text;
        this.type = type;
    }

    @Override
    public String getText() {
        return text;
    }
}
