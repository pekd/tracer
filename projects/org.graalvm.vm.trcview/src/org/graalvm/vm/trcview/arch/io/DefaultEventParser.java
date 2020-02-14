package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class DefaultEventParser extends EventParser {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> T parse(WordInputStream in, byte id, int tid) throws IOException {
        switch (id) {
            case Event.BRK:
                return (T) BrkEvent.readRecord(in, tid);
            case Event.EOF:
                return (T) new EofEvent();
            case Event.MEMORY_DUMP:
                return (T) MemoryDumpEvent.readRecord(in, tid);
            case Event.MEMORY:
                return (T) MemoryEvent.readRecord(in, tid);
            case Event.MMAP:
                return (T) MmapEvent.readRecord(in, tid);
            case Event.MUNMAP:
                return (T) MunmapEvent.readRecord(in, tid);
            case Event.MPROTECT:
                return (T) MprotectEvent.readRecord(in, tid);
            case Event.SYMBOL_TABLE:
                return (T) SymbolTableEvent.readRecord(in, tid);
            default:
                throw new IOException("unknown record type " + id);
        }
    }

    @Override
    public <T extends StepEvent> T parseStep(WordInputStream in, int tid, long step, long pc, InstructionType type, byte[] machinecode) throws IOException {
        throw new IOException("step events are not supported by this architecture");
    }

    @Override
    public <T extends InterruptEvent> T parseTrap(WordInputStream in, int tid, long step, long pc, InstructionType type, byte[] machinecode) throws IOException {
        throw new IOException("interrupt events are not supported by this architecture");
    }
}
