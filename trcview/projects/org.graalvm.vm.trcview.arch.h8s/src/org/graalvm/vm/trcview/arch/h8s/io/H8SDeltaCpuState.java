package org.graalvm.vm.trcview.arch.h8s.io;

import java.io.IOException;

import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordInputStream;

public class H8SDeltaCpuState extends H8SCpuState {
    private final H8SCpuState last;
    private final int bitfield;
    private final byte[] data;

    public H8SDeltaCpuState(long step, WordInputStream in, int bitfield, H8SCpuState last) throws IOException {
        super(step);

        this.last = last;
        this.bitfield = bitfield;

        int pclo = Short.toUnsignedInt(in.read16bit());
        int pchi = in.read8bit();
        pc = pclo | (pchi << 16);

        int size = 0;
        for (int i = 0; i < 8; i++) {
            if ((bitfield & (1 << i)) != 0) {
                size += 4;
            }
        }
        if (BitTest.test(bitfield, BIT_CCR)) {
            size++;
        }
        if (BitTest.test(bitfield, BIT_EXR)) {
            size++;
        }

        data = new byte[size];
        in.read(data);

        int machinecodeLen = (bitfield >>> 12) * 2;
        machinecode = new byte[machinecodeLen];
        in.read(machinecode);
    }

    @Override
    public int getER(int i) {
        if (BitTest.test(bitfield, 1 << i)) {
            int offset = 0;
            for (int j = 0; j < i; j++) {
                if ((bitfield & (1 << j)) != 0) {
                    offset += 4;
                }
            }
            return Endianess.get32bitLE(data, offset);
        } else {
            return last.getER(i);
        }
    }

    @Override
    public byte getCCR() {
        if (BitTest.test(bitfield, BIT_CCR)) {
            int offset = 0;
            for (int i = 0; i < 8; i++) {
                if ((bitfield & (1 << i)) != 0) {
                    offset += 4;
                }
            }
            return data[offset];
        } else {
            return last.getCCR();
        }
    }

    @Override
    public byte getEXR() {
        if (BitTest.test(bitfield, BIT_EXR)) {
            int offset = 0;
            for (int i = 0; i < 8; i++) {
                if ((bitfield & (1 << i)) != 0) {
                    offset += 4;
                }
            }
            if (BitTest.test(bitfield, BIT_CCR)) {
                offset++;
            }
            return data[offset];
        } else {
            return last.getEXR();
        }
    }
}
