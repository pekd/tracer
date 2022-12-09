package org.graalvm.vm.trcview.arch.ppc.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuDeltaState;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordInputStream;

public class PowerPCDeltaCpuState extends PowerPCCpuState implements CpuDeltaState<PowerPCCpuState> {
    private final int MASK_LR = bit(0);
    private final int MASK_CTR = bit(1);
    private final int MASK_CR = bit(2);
    private final int MASK_XER = bit(3);
    private final int MASK_FPSCR = bit(4);
    private final int MASK_SRR0 = bit(5);
    private final int MASK_SRR1 = bit(6);
    // private final int MASK_MSR = bit(5);

    private final int regmask;
    private final int gprmask;
    private final int fprmask;

    private final int insn;
    private final int pc;
    private final long step;

    private final byte[] data;

    private final PowerPCCpuState last;

    public PowerPCDeltaCpuState(WordInputStream in, int tid, PowerPCCpuState last) throws IOException {
        super(tid);
        this.last = last;
        regmask = Byte.toUnsignedInt((byte) in.read8bit());
        insn = in.read32bit();
        step = in.read64bit();
        pc = in.read32bit();
        gprmask = in.read32bit();
        fprmask = in.read32bit();

        int size = Integer.bitCount(regmask) + Integer.bitCount(gprmask) + Integer.bitCount(fprmask) * 2;
        data = new byte[size * 4];
        in.read(data);
    }

    private int getOffset(int bit, int regType) {
        int offset = 0;
        int mask;
        switch (regType) {
            case 0:
                mask = regmask;
                break;
            case 1:
                mask = gprmask;
                offset = Integer.bitCount(regmask) * 4;
                break;
            case 2:
                mask = fprmask;
                offset = (Integer.bitCount(regmask) + Integer.bitCount(gprmask)) * 4;
                break;
            default:
                throw new IllegalArgumentException("invalid type: " + regType);
        }

        if ((mask & bit) != 0) {
            for (int i = 0; i < 32; i++) {
                int b = bit(i);
                if (b == bit) {
                    return offset;
                } else if ((mask & b) != 0) {
                    offset += 4;
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

    public int getLR(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_LR, 0);
        if (offset == -1) {
            return lastState.getLR();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getCR(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_CR, 0);
        if (offset == -1) {
            return lastState.getCR();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getCTR(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_CTR, 0);
        if (offset == -1) {
            return lastState.getCTR();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getXER(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_XER, 0);
        if (offset == -1) {
            return lastState.getXER();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getFPSCR(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_FPSCR, 0);
        if (offset == -1) {
            return lastState.getFPSCR();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getGPR(int reg, PowerPCCpuState lastState) {
        int offset = getOffset(1 << reg, 1);
        if (offset == -1) {
            return lastState.getGPR(reg);
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getSRR0(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_SRR0, 0);
        if (offset == -1) {
            return lastState.getSRR0();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    public int getSRR1(PowerPCCpuState lastState) {
        int offset = getOffset(MASK_SRR1, 0);
        if (offset == -1) {
            return lastState.getSRR1();
        } else {
            return Endianess.get32bitBE(data, offset);
        }
    }

    @Override
    public int getLR() {
        return getLR(last);
    }

    @Override
    public int getCR() {
        return getCR(last);
    }

    @Override
    public int getCTR() {
        return getCTR(last);
    }

    @Override
    public int getXER() {
        return getXER(last);
    }

    @Override
    public int getFPSCR() {
        return getFPSCR(last);
    }

    @Override
    public int getGPR(int reg) {
        return getGPR(reg, last);
    }

    @Override
    public int getSRR0() {
        return getSRR0(last);
    }

    @Override
    public int getSRR1() {
        return getSRR1(last);
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return Integer.toUnsignedLong(pc);
    }

    @Override
    public PowerPCCpuState resolve(PowerPCCpuState lastState) {
        return new PowerPCFullCpuState(this, lastState);
    }
}
