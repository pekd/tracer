package org.graalvm.vm.trcview.expression;

@SuppressWarnings("serial")
public class EvaluationException extends Exception {
    public EvaluationException(String msg) {
        super(msg);
    }
}
