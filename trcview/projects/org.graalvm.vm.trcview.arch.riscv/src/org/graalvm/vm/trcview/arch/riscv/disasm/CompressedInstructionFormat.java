package org.graalvm.vm.trcview.arch.riscv.disasm;

import org.graalvm.vm.trcview.disasm.Field;
import org.graalvm.vm.trcview.disasm.Value;

public class CompressedInstructionFormat implements Value {
    public final Field OPCD = field(0, 1);

    // CR-type
    public final Field rd = field(7, 11);
    public final Field funct4 = field(12, 15);
    public final Field rs1 = field(7, 11);
    public final Field rs2 = field(2, 6);

    public final Field xop = field(10, 11);

    // CI-type
    public final Field imm6_2 = field(2, 6);
    public final Field imm12 = field(12);
    public final Field funct3 = field(13, 15);

    // CSS-type
    public final Field imm12_10 = field(10, 12);

    // CIW-type
    public final Field rd_ = field(2, 4);
    public final Field imm12_5 = field(5, 12);

    // CL-type
    public final Field imm6_5 = field(5, 6);
    public final Field rs1_ = field(7, 9);

    // CS-type
    public final Field rs2_ = field(2, 4);

    // CA-type
    public final Field funct2 = field(5, 6);
    public final Field funct6 = field(10, 15);

    // CB-type
    public final Field offset6_2 = field(2, 6);
    public final Field offset12_10 = field(10, 12);

    // CJ-type
    public final Field jumptarget = field(2, 12);

    public final Field imm12_9 = field(9, 12);
    public final Field imm8_7 = field(7, 8);
    public final Field imm9_7 = field(7, 9);

    public int value;

    public CompressedInstructionFormat() {
        this(0);
    }

    public CompressedInstructionFormat(int value) {
        this.value = value;
    }

    protected Field field(int bit) {
        return field(bit, bit);
    }

    protected Field field(int from, int to) {
        return new Field(this, 31 - to, 31 - from);
    }

    protected Field sfield(int from, int to) {
        return new Field(this, 31 - to, 31 - from, true);
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public void set(int value) {
        this.value = value;
    }
}
