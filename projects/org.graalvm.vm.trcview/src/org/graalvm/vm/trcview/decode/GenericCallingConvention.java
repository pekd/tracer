package org.graalvm.vm.trcview.decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class GenericCallingConvention extends CallingConvention {
    private static final Logger log = Trace.create(GenericCallingConvention.class);

    private List<ChangeListener> listeners = new ArrayList<>();

    private final List<Expression> fixedArguments = new ArrayList<>();
    private Expression stackArguments;
    private Expression callReturn;

    protected void fireChangeEvent() {
        for (ChangeListener l : listeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.log(Levels.WARNING, "ChangeListener failed: " + t, t);
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void setReturn(Expression expr) {
        callReturn = expr;
        fireChangeEvent();
    }

    public void setStack(Expression expr) {
        stackArguments = expr;
        fireChangeEvent();
    }

    public Expression getStack() {
        return stackArguments;
    }

    public void setArguments(List<Expression> expr) {
        fixedArguments.clear();
        fixedArguments.addAll(expr);
        fireChangeEvent();
    }

    public List<Expression> getArguments() {
        return Collections.unmodifiableList(fixedArguments);
    }

    @Override
    public int getFixedArgumentCount() {
        return fixedArguments.size();
    }

    @Override
    public Expression getArgument(int i) {
        if (i >= getFixedArgumentCount()) {
            return null;
        } else {
            return fixedArguments.get(i);
        }
    }

    @Override
    public Expression getReturn() {
        return callReturn;
    }

    @Override
    public List<Expression> getArguments(Prototype proto) {
        List<Expression> result = new ArrayList<>();
        long totalsize = 0;
        long id = 0;
        Map<String, Long> vars = new HashMap<>();
        for (int i = 0, j = 0; i < proto.args.size(); i++) {
            Type arg = proto.args.get(i);
            if (arg.getExpression() == null) {
                if (j < getFixedArgumentCount()) {
                    result.add(getArgument(j++));
                } else {
                    vars.put("arg_totalsize", totalsize);
                    vars.put("arg_id", id++);
                    vars.put("arg_size", (long) arg.getSize());
                    Expression expr = stackArguments.materialize(vars);
                    totalsize += arg.getSize();
                    result.add(expr);
                }
            } else {
                result.add(arg.getExpression());
            }
        }
        return result;
    }
}
