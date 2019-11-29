package org.graalvm.vm.trcview.expression;

@SuppressWarnings("serial")
public class UnknownVariableException extends EvaluationException {
    private String var;

    public UnknownVariableException(String var) {
        super("Unknown variable '" + var + "'");
        this.var = var;
    }

    public String getVariable() {
        return var;
    }
}
