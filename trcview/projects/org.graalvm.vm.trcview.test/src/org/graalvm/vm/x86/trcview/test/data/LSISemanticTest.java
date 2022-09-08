package org.graalvm.vm.x86.trcview.test.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.graalvm.vm.trcview.arch.pdp11.disasm.PDP11Disassembler;
import org.graalvm.vm.trcview.arch.pdp11.disasm.PDP11Semantics;
import org.graalvm.vm.trcview.data.SemanticInfo;
import org.graalvm.vm.trcview.data.SemanticInfo.Operation;
import org.graalvm.vm.trcview.data.ir.ConstOperand;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.junit.Test;

public class LSISemanticTest {
    @Test
    public void testBIT() {
        short[] insn = {032737, 000200, (short) 0177560};
        short pc = 05142;

        assertEquals(3, PDP11Disassembler.getLength(insn));
        assertArrayEquals(new String[]{"BIT", "#200", "@#177560"}, PDP11Disassembler.getDisassembly(insn, pc));

        SemanticInfo info = new SemanticInfo();
        assertArrayEquals(new int[0], PDP11Semantics.getRegisterReads(insn, pc));
        assertArrayEquals(new int[0], PDP11Semantics.getRegisterWrites(insn, pc));

        PDP11Semantics.getSemantics(info, insn, pc);
        List<Operation> ops = info.getRawOperations();
        assertEquals(2, ops.size());

        // the #200 operand
        Operation srcop = ops.get(0);
        assertEquals(SemanticInfo.OP_CONSTRAINT, srcop.getOp());

        assertTrue(srcop.getDestination() instanceof ConstOperand);
        assertEquals(VariableType.I16, srcop.getType());

        // the memory operand @#177560
        Operation dstop = ops.get(1);
        assertEquals(SemanticInfo.OP_CONSTRAINT, dstop.getOp());

        Operand dst = dstop.getDestination();
        assertTrue(dst instanceof MemoryOperand);
        MemoryOperand mem = (MemoryOperand) dst;
        assertEquals(0177560, mem.getAddress());
        assertEquals(VariableType.I16, dstop.getType());
    }
}
