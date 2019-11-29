package org.graalvm.vm.trcview.analysis.memory;

import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class VirtualMemorySnapshot {
    private TraceAnalyzer trc;
    private long insn;

    public VirtualMemorySnapshot(TraceAnalyzer trc, long insn) {
        this.trc = trc;
        this.insn = insn;
    }

    public byte getI8(long addr) throws MemoryNotMappedException {
        return trc.getI8(addr, insn);
    }

    public long getI64(long addr) throws MemoryNotMappedException {
        // TODO: remove assertion code
        long val1 = trc.getI64(addr, insn);
        long val2 = 0;
        for (int i = 0; i < 8; i++) {
            val2 >>>= 8;
            val2 |= Byte.toUnsignedLong(trc.getI8(addr + i, insn)) << 56;
        }
        assert val1 == val2 : String.format("getWord(0x%016x) = 0x%016x vs getByte -> 0x%016x", addr, val1, val2);
        // END TODO
        return trc.getI64(addr, insn);
    }
}
