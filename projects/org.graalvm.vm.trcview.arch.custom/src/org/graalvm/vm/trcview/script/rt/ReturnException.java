package org.graalvm.vm.trcview.script.rt;

@SuppressWarnings("serial")
public class ReturnException extends ControlFlowException {
    private final long value;
    private final Pointer ptr;

    public ReturnException() {
        this(0);
    }

    public ReturnException(long value) {
        this.value = value;
        this.ptr = null;
    }

    public ReturnException(Pointer value) {
        this.value = 0;
        this.ptr = value;
    }

    public long getValue() {
        return value;
    }

    public Pointer getPointer() {
        return ptr;
    }
}
