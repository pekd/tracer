package org.graalvm.vm.trcview.decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class GenericCallingConvention extends CallingConvention {
    private static final Logger log = Trace.create(GenericCallingConvention.class);

    private List<ChangeListener> listeners = new ArrayList<>();

    private final List<Expression> callArguments = new ArrayList<>();
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

    public void setArguments(List<Expression> expr) {
        callArguments.clear();
        callArguments.addAll(expr);
        fireChangeEvent();
    }

    public List<Expression> getArguments() {
        return Collections.unmodifiableList(callArguments);
    }

    @Override
    public int getFixedArgumentCount() {
        return callArguments.size();
    }

    @Override
    public Expression getArgument(int i) {
        return callArguments.get(i);
    }

    @Override
    public Expression getReturn() {
        return callReturn;
    }
}
