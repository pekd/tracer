package org.graalvm.vm.trcview.arch.h8s.io;

import java.io.IOException;

import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.io.WordInputStream;

public class H8SSmallSPRDeltaCpuState extends H8SCpuState {
    private final H8SCpuState last;

    private final byte ccr;
    private final byte exr;

    public H8SSmallSPRDeltaCpuState(long step, WordInputStream in, int bitfield, H8SCpuState last) throws IOException {
        super(step);

        this.last = last;

        int pclo = Short.toUnsignedInt(in.read16bit());
        int pchi = in.read8bit();
        pc = pclo | (pchi << 16);

        assert (bitfield & 0xFF) == 0;

        if (BitTest.test(bitfield, BIT_CCR)) {
            ccr = (byte) in.read8bit();
        } else {
            ccr = last.getCCR();
        }

        if (BitTest.test(bitfield, BIT_EXR)) {
            exr = (byte) in.read8bit();
        } else {
            exr = last.getEXR();
        }

        int machinecodeLen = (bitfield >>> 12) * 2;
        machinecode = new byte[machinecodeLen];
        in.read(machinecode);
    }

    @Override
    public int getER(int i) {
        return last.getER(i);
    }

    @Override
    public byte getCCR() {
        return ccr;
    }

    @Override
    public byte getEXR() {
        return exr;
    }
}
