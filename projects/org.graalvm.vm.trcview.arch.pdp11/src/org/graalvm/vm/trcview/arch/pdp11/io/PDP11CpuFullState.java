package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;

public class PDP11CpuFullState extends PDP11CpuState {
    private final short[] registers = new short[8];
    private final short psw;
    private final long step;
    private final short[] insn;

    public PDP11CpuFullState(WordInputStream in, int tid) throws IOException {
        super(tid);
        for (int i = 0; i < 8; i++) {
            registers[i] = in.read16bit();
        }
        psw = in.read16bit();
        insn = new short[3];
        for (int i = 0; i < 3; i++) {
            insn[i] = in.read16bit();
        }
        in.read32bit();
        step = in.read64bit();
    }

    public PDP11CpuFullState(PDP11CpuState state) {
        super(state.getTid());
        for (int i = 0; i < 8; i++) {
            registers[i] = state.getRegister(i);
        }
        psw = state.getPSW();
        insn = state.getMachinecode();
        step = state.getStep();
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public short[] getMachinecode() {
        return insn;
    }

    @Override
    public short getPSW() {
        return psw;
    }

    @Override
    public short getRegister(int i) {
        return registers[i];
    }
}
