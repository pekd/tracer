package org.graalvm.vm.trcview.arch.h8s.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class H8STinyDeltaCpuState extends H8SCpuState {
    private final H8SCpuState last;

    public H8STinyDeltaCpuState(long step, WordInputStream in, int bitfield, H8SCpuState last) throws IOException {
        super(step);

        this.last = last;

        int pclo = Short.toUnsignedInt(in.read16bit());
        int pchi = in.read8bit();
        pc = pclo | (pchi << 16);

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
        return last.getCCR();
    }

    @Override
    public byte getEXR() {
        return last.getEXR();
    }
}
