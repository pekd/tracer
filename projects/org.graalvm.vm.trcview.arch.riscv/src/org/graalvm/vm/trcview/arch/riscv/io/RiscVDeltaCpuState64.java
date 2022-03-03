package org.graalvm.vm.trcview.arch.riscv.io;

import java.io.IOException;

import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordInputStream;

public class RiscVDeltaCpuState64 extends RiscVCpuState {
    private final int gprmask;

    private final int insn;
    private final long pc;
    private final long step;

    private final byte[] data;

    private final RiscVCpuState last;

    public RiscVDeltaCpuState64(WordInputStream in, int tid, RiscVCpuState last) throws IOException {
        super(tid);
        this.last = last;
        insn = in.read32bit();
        gprmask = in.read32bit();
        step = in.read64bit();
        pc = in.read64bit();

        int size = Integer.bitCount(gprmask);
        data = new byte[size * 8];
        in.read(data);
    }

    private int getOffset(int bit) {
        int offset = 0;
        int mask = gprmask;

        if ((mask & bit) != 0) {
            for (int i = 0; i < 32; i++) {
                int b = bit(i);
                if (b == bit) {
                    return offset;
                } else if ((mask & b) != 0) {
                    offset += 8;
                }
            }
            throw new AssertionError("this should be unreachable");
        } else {
            return -1;
        }
    }

    private static int bit(int i) {
        return 1 << i;
    }

    @Override
    public int getInstruction() {
        return insn;
    }

    @Override
    public long getGPR(int reg) {
        int offset = getOffset(1 << reg);
        if (offset == -1) {
            return last.getGPR(reg);
        } else {
            return Endianess.get64bitLE(data, offset);
        }
    }

    @Override
    public long getCSR(int reg) {
        return 0;
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return pc;
    }
}
