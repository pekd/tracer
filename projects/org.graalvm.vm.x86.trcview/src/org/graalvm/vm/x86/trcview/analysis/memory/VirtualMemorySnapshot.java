package org.graalvm.vm.x86.trcview.analysis.memory;

public class VirtualMemorySnapshot {
    private MemoryTrace trace;
    private long insn;

    public VirtualMemorySnapshot(MemoryTrace trace, long insn) {
        this.trace = trace;
        this.insn = insn;
    }

    public byte getI8(long addr) throws MemoryNotMappedException {
        return trace.getByte(addr, insn);
    }

    public long getI64(long addr) throws MemoryNotMappedException {
        // TODO: remove assertion code
        long val1 = trace.getWord(addr, insn);
        long val2 = 0;
        for (int i = 0; i < 8; i++) {
            val2 >>>= 8;
            val2 |= Byte.toUnsignedLong(trace.getByte(addr + i, insn)) << 56;
        }
        assert val1 == val2 : String.format("getWord(0x%016x) = 0x%016x vs getByte -> 0x%016x", addr, val1, val2);
        // END TODO
        return trace.getWord(addr, insn);
    }
}
