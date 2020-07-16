package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class PDP11CpuDeltaState extends PDP11CpuState {
    // bits 0-6 = r0 - r6
    private static final int BIT_PSW = 7;

    private final PDP11CpuState previous;
    private final short[] data;

    private final short mask;
    private final short pc;
    private final long step;
    private final short[] insn = new short[3];

    public PDP11CpuDeltaState(WordInputStream in, PDP11CpuState previous, int tid) throws IOException {
        super(tid);
        this.previous = previous;

        pc = in.read16bit();
        mask = in.read16bit();
        if (mask < 0) {
            step = in.read64bit();
        } else {
            step = Integer.toUnsignedLong(in.read32bit());
        }
        int cnt = (mask >> 8) & 3;
        for (int i = 0; i < cnt; i++) {
            insn[i] = in.read16bit();
        }
        cnt = 0;
        for (int i = 0; i < 8; i++) {
            if (has(i)) {
                cnt++;
            }
        }
        data = new short[cnt];
        for (int i = 0; i < data.length; i++) {
            data[i] = in.read16bit();
        }
    }

    private boolean has(int bit) {
        return (mask & (1 << bit)) != 0;
    }

    private int offset(int bit) {
        assert has(bit);
        if (bit == 0) {
            return 0;
        } else {
            int off = 0;
            for (int i = 0; i < bit; i++) {
                if (has(i)) {
                    off++;
                }
            }
            return off;
        }
    }

    @Override
    public short getRegister(int id) {
        if (id == 7) {
            return pc;
        } else if (has(id)) {
            return data[offset(id)];
        } else {
            return previous.getRegister(id);
        }
    }

    @Override
    public short getPSW() {
        if (has(BIT_PSW)) {
            return data[offset(BIT_PSW)];
        } else {
            return previous.getPSW();
        }
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public short[] getMachinecodeWords() {
        return insn;
    }
}
