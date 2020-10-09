package org.graalvm.vm.trcview.ui.data;

import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.EditorModel;
import org.graalvm.vm.trcview.ui.data.editor.Line;

public class DataViewModel extends EditorModel {
    private TraceAnalyzer trc;
    private long step;

    private int maxlen = 80;

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        fireChangeEvent();
    }

    public void setStep(long step) {
        this.step = step;
        fireChangeEvent();
    }

    @Override
    public int getLineCount() {
        return 65536;
    }

    public long getAddressByLine(long line) {
        return line;
    }

    public long getLineByAddress(long addr) {
        return addr;
    }

    @Override
    public Line getLine(int line) {
        long addr = getAddressByLine(line);

        Line l = new DataLine(addr, 1, step, trc);
        if (l != null) {
            int len = l.getLength();
            if (len > maxlen) {
                maxlen = len;
            }
        }
        return l;
    }

    @Override
    public int getMaximumLineLength() {
        return maxlen;
    }
}
