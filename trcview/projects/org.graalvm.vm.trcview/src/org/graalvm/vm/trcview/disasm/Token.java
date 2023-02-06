package org.graalvm.vm.trcview.disasm;

public class Token {
    private Type type;
    private String text;
    private long value;

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
        this.value = 0;
    }

    public Token(Type type, long value) {
        this.type = type;
        this.text = null;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public long getValue() {
        return value;
    }

    public boolean hasValue() {
        return text == null;
    }

    @Override
    public String toString() {
        if (hasValue()) {
            return Long.toUnsignedString(getValue());
        } else {
            return text;
        }
    }
}
