package org.graalvm.vm.x86.trcview.test.data;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.data.DynamicTypePropagation;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.junit.Test;

public class LSITypeTests extends LSIRunner {
    @Test
    public void testStraightline() throws IOException {
        Analysis analysis = load("resources/straightline.trc");
        DynamicTypePropagation types = analysis.getTypeRecovery();

        // 01000: MOV #177566, R0
        long r0 = types.getSemantics().resolve(01000, new RegisterOperand(0));
        check(r0, VariableType.I16);

        // 01004: MOVB #101, R1
        long r1 = types.getSemantics().resolve(01004, new RegisterOperand(1));
        check(r1, VariableType.I8);

        // 01010: MOVB R1, R2
        long r2 = types.getSemantics().resolve(01010, new RegisterOperand(2));
        check(r2, VariableType.I8);

        // 01012: MOV R0, R3
        long r3 = types.getSemantics().resolve(01012, new RegisterOperand(3));
        check(r3, VariableType.I16);

        // 01014: MOV #123456, R5
        long r5 = types.getSemantics().resolve(01014, new RegisterOperand(5));
        check(r5, VariableType.I16);

        // 01020: MOVB #102, R4
        long r4 = types.getSemantics().resolve(01020, new RegisterOperand(4));
        check(r4, VariableType.I8);

        // 01024: MOV R4, R5
        r5 = types.getSemantics().resolve(01024, new RegisterOperand(5));
        check(r5, VariableType.I8);
    }
}
