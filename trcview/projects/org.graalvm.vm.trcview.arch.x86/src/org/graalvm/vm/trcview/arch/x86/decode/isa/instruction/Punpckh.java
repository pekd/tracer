/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.trcview.arch.x86.decode.isa.instruction;

import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64Instruction;
import org.graalvm.vm.trcview.arch.x86.decode.isa.Operand;
import org.graalvm.vm.trcview.arch.x86.decode.isa.OperandDecoder;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;

public abstract class Punpckh extends AMD64Instruction {
    private final String name;
    private final Operand operand1;
    private final Operand operand2;

    protected Punpckh(long pc, byte[] instruction, String name, Operand operand1, Operand operand2) {
        super(pc, instruction);
        this.name = name;
        this.operand1 = operand1;
        this.operand2 = operand2;

        setGPRReadOperands(operand1, operand2);
        setGPRWriteOperands(operand1);
    }

    public static class Punpckhbw extends Punpckh {
        public Punpckhbw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, "punpckhbw", operands.getAVXOperand2(128), operands.getAVXOperand1(128));
        }
    }

    public static class Punpckhwd extends Punpckh {
        public Punpckhwd(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, "punpckhwd", operands.getAVXOperand2(128), operands.getAVXOperand1(128));
        }
    }

    public static class Punpckhdq extends Punpckh {
        public Punpckhdq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, "punpckhdq", operands.getAVXOperand2(128), operands.getAVXOperand1(128));
        }
    }

    public static class Punpckhqdq extends Punpckh {
        public Punpckhqdq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, "punpckhqdq", operands.getAVXOperand2(128), operands.getAVXOperand1(128));
        }
    }

    @Override
    protected AssemblerInstruction disassemble() {
        return new AssemblerInstruction(name, operand1, operand2);
    }
}
