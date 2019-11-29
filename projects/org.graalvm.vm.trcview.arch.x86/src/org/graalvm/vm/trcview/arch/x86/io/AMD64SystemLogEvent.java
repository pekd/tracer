package org.graalvm.vm.trcview.arch.x86.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.SystemLogEvent;
import org.graalvm.vm.trcview.arch.x86.AMD64;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.node.debug.trace.SystemLogRecord;

public class AMD64SystemLogEvent extends SystemLogEvent {
    private final String text;

    public AMD64SystemLogEvent(SystemLogRecord record) {
        super(AMD64.ID, record.getTid());
        text = record.toString();
    }

    private AMD64SystemLogEvent(int tid, String text) {
        super(AMD64.ID, tid);
        this.text = text;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        IO.writeString(out, text);
    }

    public static AMD64SystemLogEvent readRecord(WordInputStream in, int tid) throws IOException {
        String text = IO.readString(in);
        return new AMD64SystemLogEvent(tid, text);
    }
}
