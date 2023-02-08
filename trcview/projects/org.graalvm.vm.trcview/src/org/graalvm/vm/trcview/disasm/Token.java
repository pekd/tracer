package org.graalvm.vm.trcview.disasm;

public class Token {
    private Type type;
    private String text;
    private long value;
    private boolean hasValue;

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
        this.value = 0;
        hasValue = false;
    }

    public Token(Type type, long value) {
        this.type = type;
        this.text = null;
        this.value = value;
        hasValue = true;
    }

    public Token(Type type, String text, long value) {
        this.type = type;
        this.text = text;
        this.value = value;
        hasValue = true;
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
        return hasValue;
    }

    @Override
    public String toString() {
        if (text == null) {
            return Long.toUnsignedString(getValue());
        } else {
            return text;
        }
    }
}
