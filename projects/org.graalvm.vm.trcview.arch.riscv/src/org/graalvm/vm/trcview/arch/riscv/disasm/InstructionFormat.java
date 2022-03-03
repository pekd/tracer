package org.graalvm.vm.trcview.arch.riscv.disasm;

import org.graalvm.vm.trcview.disasm.Field;
import org.graalvm.vm.trcview.disasm.Value;

public class InstructionFormat implements Value {
    public final Field OPCD = field(0, 6);

    // R-type
    public final Field rd = field(7, 11);
    public final Field funct3 = field(12, 14);
    public final Field rs1 = field(15, 19);
    public final Field rs2 = field(20, 24);
    public final Field funct7 = field(25, 31);

    // I-type
    public final Field imm11_0 = sfield(20, 31);
    public final Field imm11_0u = field(20, 31);

    // S-type
    public final Field imm4_0 = field(7, 11);
    public final Field imm11_5 = field(25, 31);

    // U-type
    public final Field imm31_12 = field(12, 31);

    // B-type
    public final Field imm11_B = field(7);
    public final Field imm4_1 = field(8, 11);
    public final Field imm10_5 = field(25, 30);
    public final Field imm12 = field(31);

    // J-type
    public final Field imm19_12 = field(12, 19);
    public final Field imm11_J = field(20);
    public final Field imm10_1 = field(21, 30);
    public final Field imm20 = field(31);

    public int value;

    public InstructionFormat() {
        this(0);
    }

    public InstructionFormat(int value) {
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
