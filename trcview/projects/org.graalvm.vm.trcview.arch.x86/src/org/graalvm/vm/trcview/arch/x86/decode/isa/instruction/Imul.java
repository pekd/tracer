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
import org.graalvm.vm.trcview.arch.x86.decode.isa.ImmediateOperand;
import org.graalvm.vm.trcview.arch.x86.decode.isa.Operand;
import org.graalvm.vm.trcview.arch.x86.decode.isa.OperandDecoder;
import org.graalvm.vm.trcview.arch.x86.decode.isa.Register;
import org.graalvm.vm.trcview.arch.x86.decode.isa.RegisterOperand;

public abstract class Imul extends AMD64Instruction {
    protected final Operand operand1;
    protected final Operand operand2;
    protected final Operand operand3;

    protected Imul(long pc, byte[] instruction, Operand operand1) {
        this(pc, instruction, operand1, null, null);
    }

    protected Imul(long pc, byte[] instruction, Operand operand1, Operand operand2) {
        this(pc, instruction, operand1, operand2, null);
    }

    protected Imul(long pc, byte[] instruction, Operand operand1, Operand operand2, Operand operand3) {
        super(pc, instruction);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
    }

    private static abstract class Imul1 extends Imul {
        protected Imul1(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, operands.getOperand1(type));

            setGPRReadOperands(operand1, new RegisterOperand(Register.RAX));
            setGPRWriteOperands(new RegisterOperand(Register.RAX), new RegisterOperand(Register.RDX));
        }
    }

    public static class Imul1b extends Imul1 {
        public Imul1b(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R8);
            setGPRWriteOperands(new RegisterOperand(Register.RAX));
        }
    }

    public static class Imul1w extends Imul1 {
        public Imul1w(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Imul1l extends Imul1 {
        public Imul1l(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Imul1q extends Imul1 {
        public Imul1q(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    private static abstract class Imul2 extends Imul {
        private final Operand srcA;
        private final Operand srcB;
        private final Operand dst;

        protected Imul2(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, operands.getOperand2(type), operands.getOperand1(type));
            this.dst = operand1;
            this.srcA = operand1;
            this.srcB = operand2;

            setGPRReadOperands(srcA, srcB);
            setGPRWriteOperands(dst);
        }

        protected Imul2(long pc, byte[] instruction, OperandDecoder operands, short imm, int type) {
            super(pc, instruction, operands.getOperand2(type), operands.getOperand1(type), new ImmediateOperand(imm));
            this.dst = operand1;
            this.srcA = operand2;
            this.srcB = operand3;

            setGPRReadOperands(srcA, srcB);
            setGPRWriteOperands(dst);
        }

        protected Imul2(long pc, byte[] instruction, OperandDecoder operands, int imm, int type) {
            super(pc, instruction, operands.getOperand2(type), operands.getOperand1(type), new ImmediateOperand(imm));
            this.dst = operand1;
            this.srcA = operand2;
            this.srcB = operand3;

            setGPRReadOperands(srcA, srcB);
            setGPRWriteOperands(dst);
        }

        protected Imul2(long pc, byte[] instruction, OperandDecoder operands, long imm, int type) {
            super(pc, instruction, operands.getOperand2(type), operands.getOperand1(type), new ImmediateOperand(imm));
            this.dst = operand1;
            this.srcA = operand2;
            this.srcB = operand3;

            setGPRReadOperands(srcA, srcB);
            setGPRWriteOperands(dst);
        }
    }

    public static class Imulw extends Imul2 {
        public Imulw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }

        public Imulw(long pc, byte[] instruction, OperandDecoder operands, short imm) {
            super(pc, instruction, operands, imm, OperandDecoder.R16);
        }
    }

    public static class Imull extends Imul2 {
        public Imull(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }

        public Imull(long pc, byte[] instruction, OperandDecoder operands, int imm) {
            super(pc, instruction, operands, imm, OperandDecoder.R32);
        }
    }

    public static class Imulq extends Imul2 {
        public Imulq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }

        public Imulq(long pc, byte[] instruction, OperandDecoder operands, long imm) {
            super(pc, instruction, operands, imm, OperandDecoder.R64);
        }
    }

    @Override
    protected String[] disassemble() {
        if (operand3 != null) {
            return new String[]{"imul", operand1.toString(), operand2.toString(), operand3.toString()};
        } else if (operand2 != null) {
            return new String[]{"imul", operand1.toString(), operand2.toString()};
        } else {
            return new String[]{"imul", operand1.toString()};
        }
    }
}
