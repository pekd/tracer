package org.graalvm.vm.x86.trcview.expression;

@SuppressWarnings("serial")
public class ArityException extends EvaluationException {
    private int expected;
    private int received;

    public ArityException(int expected, int received) {
        super("Expected " + expected + " arguments, got " + received);
        this.expected = expected;
        this.received = received;
    }

    public int getExpected() {
        return expected;
    }

    public int getReceived() {
        return received;
    }
}
