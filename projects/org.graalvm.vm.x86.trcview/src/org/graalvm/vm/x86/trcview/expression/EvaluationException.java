package org.graalvm.vm.x86.trcview.expression;

@SuppressWarnings("serial")
public class EvaluationException extends Exception {
    public EvaluationException(String msg) {
        super(msg);
    }
}
