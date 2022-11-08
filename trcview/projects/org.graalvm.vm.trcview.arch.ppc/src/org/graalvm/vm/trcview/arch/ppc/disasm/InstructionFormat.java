package org.graalvm.vm.trcview.arch.ppc.disasm;

import org.graalvm.vm.trcview.disasm.Field;
import org.graalvm.vm.trcview.disasm.Value;

public class InstructionFormat implements Value {
    public final Field OPCD = field(0, 5);

    // I-FORM
    public final Field LI = sfield(6, 29);
    public final Field AA = field(30);
    public final Field LK = field(31);

    // B-FORM
    public final Field BO = field(6, 10);
    public final Field BI = field(11, 15);
    public final Field BD = sfield(16, 29);
    // AA(30)
    // LK(31)

    // SC-FORM:
    public final Field LEV = field(20, 26);
    // 1(30)

    // D-FORM
    public final Field RT = field(6, 10);
    public final Field RS = field(6, 10);
    public final Field BF = field(6, 10);
    public final Field TO = field(6, 10);
    public final Field FRT = field(6, 10);
    public final Field FRS = field(6, 10);
    public final Field RA = field(11, 15);
    public final Field D = sfield(16, 31);
    public final Field SI = sfield(16, 31);
    public final Field UI = field(16, 31);
    public final Field L = field(10);

    // DS-FORM
    public final Field RSp = field(6, 10);
    public final Field FRTp = field(6, 10);
    public final Field FRSp = field(6, 10);
    public final Field DS = field(16, 29);
    public final Field XO_0 = field(30, 31);

    // DQ-FORM
    public final Field RTp = field(6, 10);
    public final Field DQ = field(16, 27);

    // X-FORM
    public final Field SR = field(11, 15);
    public final Field FRA = field(11, 15);
    public final Field BRA = field(11, 15);
    public final Field BFA = field(11, 15);
    public final Field RB = field(16, 20);
    public final Field XO_1 = field(21, 30);
    public final Field X_L = field(9);
    public final Field X_BF = field(6, 8);
    public final Field E = field(12, 15);
    public final Field TH = field(6, 10);
    public final Field EH = field(31);
    public final Field FRB = field(16, 20);
    public final Field VRS = field(6, 10);
    public final Field VRT = field(6, 10);
    public final Field NB = field(16, 20);

    public final Field W = field(15);
    public final Field U = field(16, 19);

    // XL-FORM
    public final Field BT = field(6, 10);
    public final Field BA = field(11, 15);
    public final Field BB = field(16, 20);
    public final Field BH = field(19);
    public final Field S = field(20);
    public final Field OC = field(6, 20);
    public final Field XL_BF = field(6, 8);
    public final Field XL_BFA = field(11, 13);

    // ...

    // XFL-FORM
    public final Field XFL_L = field(6);
    public final Field FLM = field(7, 14);

    // XFX-FORM
    public final Field spr = field(11, 20);
    public final Field tbr = field(11, 20);
    public final Field dcr = field(11, 20);
    public final Field pmrn = field(11, 20);
    public final Field DUIS = field(11, 20);
    public final Field FXM = field(12, 19);
    public final Field BIT_11 = field(11);

    // XO-FORM
    public final Field OE = field(21);
    public final Field XO_2 = field(22, 30);
    public final Field Rc = field(31);

    // ...

    // XX3-FORM
    public final Field T = field(6, 10);
    public final Field A = field(11, 15);
    public final Field B = field(16, 20);
    public final Field XXO = field(21, 28);
    public final Field AX = field(29);
    public final Field BX = field(30);
    public final Field TX = field(31);

    // A-FORM:
    public final Field FRC = field(21, 25);
    public final Field XO_6 = field(26, 30);
    public final Field BC = field(21, 25);

    // M-FORM
    public final Field SH = field(16, 20);
    public final Field MB = field(21, 25);
    public final Field ME = field(26, 30);

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
        return new Field(this, from, to);
    }

    protected Field sfield(int from, int to) {
        return new Field(this, from, to, true);
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
