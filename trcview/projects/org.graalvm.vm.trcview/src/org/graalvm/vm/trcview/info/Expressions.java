package org.graalvm.vm.trcview.info;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class Expressions {
    private final Map<Long, FormattedExpression> expressions;

    public Expressions() {
        expressions = new HashMap<>();
    }

    public FormattedExpression getExpression(long pc) {
        return expressions.get(pc);
    }

    public void setExpression(long pc, StepFormat format, String expr) throws ParseException {
        if (expr == null) {
            expressions.remove(pc);
        } else {
            expressions.put(pc, new FormattedExpression(format, expr));
        }
    }

    public String evaluate(CpuState state, TraceAnalyzer trc) throws EvaluationException {
        FormattedExpression expr = expressions.get(state.getPC());
        if (expr != null) {
            return expr.evaluate(state, trc);
        } else {
            return null;
        }
    }

    public Map<Long, String> getExpressions() {
        Map<Long, String> result = new HashMap<>();
        expressions.entrySet().forEach(e -> result.put(e.getKey(), e.getValue().getExpression()));
        return result;
    }
}
