package org.graalvm.vm.trcview.arch.h8s.io;

public class H8SZeroCpuState extends H8SCpuState {
    public H8SZeroCpuState(long step) {
        super(step);

        pc = 0;
        machinecode = new byte[0];
    }

    @Override
    public int getER(int i) {
        return 0;
    }

    @Override
    public byte getCCR() {
        return 0;
    }

    @Override
    public byte getEXR() {
        return 0;
    }
}
