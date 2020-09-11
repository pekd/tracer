package org.graalvm.vm.trcview.decode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public abstract class ABI {
    private static final Logger log = Trace.create(ABI.class);

    private List<ChangeListener> listeners = new ArrayList<>();

    public abstract Function getSyscall(long id);

    public abstract Expression getSyscallId();

    public abstract CallingConvention getCall();

    public abstract CallingConvention getSyscall();

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChangeEvent() {
        for (ChangeListener l : listeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.log(Levels.WARNING, "ChangeListener failed: " + t, t);
            }
        }
    }
}
