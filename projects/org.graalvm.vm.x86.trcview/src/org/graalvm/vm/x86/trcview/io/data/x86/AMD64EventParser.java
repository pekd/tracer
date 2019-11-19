package org.graalvm.vm.x86.trcview.io.data.x86;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.x86.trcview.io.data.Event;
import org.graalvm.vm.x86.trcview.io.data.EventParser;

public class AMD64EventParser extends EventParser {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException {
        switch (id) {
            case Event.CPU_STATE:
                return (T) AMD64CpuState.readRecord(in, tid);
            case Event.STEP:
                return (T) AMD64StepEvent.readRecord(in, tid);
            case Event.SYSTEM_LOG:
                return (T) AMD64SystemLogEvent.readRecord(in, tid);
            default:
                throw new IOException("unknown record type " + id);
        }
    }
}
