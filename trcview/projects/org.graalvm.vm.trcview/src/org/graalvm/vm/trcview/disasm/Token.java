package org.graalvm.vm.trcview.disasm;

public class Token {
    private Type type;
    private String text;
    private long value;
    private boolean hasValue;

    private long pc;
    private boolean pcRelative;

    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
        this.value = 0;
        hasValue = false;
        pcRelative = false;
    }

    public Token(Type type, long value) {
        this.type = type;
        this.text = null;
        this.value = value;
        hasValue = true;
        pcRelative = false;
    }

    public Token(Type type, String text, long value) {
        this.type = type;
        this.text = text;
        this.value = value;
        hasValue = true;
        pcRelative = false;
    }

    public Token(Type type, String text, long value, boolean pcRelative) {
        this.type = type;
        this.text = text;
        this.value = value;
        hasValue = true;
        this.pcRelative = pcRelative;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public long getValue() {
        if (pcRelative) {
            return value + pc;
        } else {
            return value;
        }
    }

    public boolean hasValue() {
        return hasValue;
    }

    public void setPC(long pc) {
        this.pc = pc;
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
