package org.graalvm.vm.trcview.ui.data.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public abstract class EditorModel {
    private static final Logger log = Trace.create(EditorModel.class);

    private final List<ChangeListener> listeners = new ArrayList<>();

    public abstract int getLineCount();

    public abstract Line getLine(int line);

    public abstract int getMaximumLineLength();

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
                log.log(Levels.WARNING, "Exception in event handler: " + t, t);
            }
        }
    }
}
