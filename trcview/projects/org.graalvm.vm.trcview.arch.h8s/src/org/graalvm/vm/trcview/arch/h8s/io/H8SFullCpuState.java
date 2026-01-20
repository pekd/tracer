package org.graalvm.vm.trcview.arch.h8s.io;

public class H8SFullCpuState extends H8SCpuState {
    private final int[] er;
    private final byte ccr;
    private final byte exr;

    public H8SFullCpuState(H8SCpuState last) {
        super(last.getStep());
        pc = (int) last.getPC();
        machinecode = last.getMachinecode();
        er = new int[8];
        for (int i = 0; i < 8; i++) {
            er[i] = last.getER(i);
        }
        ccr = last.getCCR();
        exr = last.getEXR();
    }

    @Override
    public int getER(int i) {
        return er[i];
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
