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

public abstract class Adc extends AMD64Instruction {
    private final Operand operand1;
    private final Operand operand2;

    protected static Operand getOp1(OperandDecoder operands, int type, boolean swap) {
        if (swap) {
            return operands.getOperand2(type);
        } else {
            return operands.getOperand1(type);
        }
    }

    protected static Operand getOp2(OperandDecoder operands, int type, boolean swap) {
        if (swap) {
            return operands.getOperand1(type);
        } else {
            return operands.getOperand2(type);
        }
    }

    protected Adc(long pc, byte[] instruction, Operand operand1, Operand operand2) {
        super(pc, instruction);
        this.operand1 = operand1;
        this.operand2 = operand2;

        setGPRReadOperands(operand1, operand2);
        setGPRWriteOperands(operand1);
    }

    public static class Adcb extends Adc {
        public Adcb(long pc, byte[] instruction, OperandDecoder operands) {
            this(pc, instruction, operands, false);
        }

        public Adcb(long pc, byte[] instruction, OperandDecoder operands, boolean swap) {
            super(pc, instruction, getOp1(operands, OperandDecoder.R8, swap), getOp2(operands, OperandDecoder.R8, swap));
        }

        public Adcb(long pc, byte[] instruction, OperandDecoder operands, byte imm) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R8), new ImmediateOperand(imm));
        }

        public Adcb(long pc, byte[] instruction, Operand operand, byte imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    public static class Adcw extends Adc {
        public Adcw(long pc, byte[] instruction, OperandDecoder operands) {
            this(pc, instruction, operands, false);
        }

        public Adcw(long pc, byte[] instruction, OperandDecoder operands, boolean swap) {
            super(pc, instruction, getOp1(operands, OperandDecoder.R16, swap), getOp2(operands, OperandDecoder.R16, swap));
        }

        public Adcw(long pc, byte[] instruction, OperandDecoder operands, short imm) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R16), new ImmediateOperand(imm));
        }

        public Adcw(long pc, byte[] instruction, Operand operand, short imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    public static class Adcl extends Adc {
        public Adcl(long pc, byte[] instruction, OperandDecoder operands) {
            this(pc, instruction, operands, false);
        }

        public Adcl(long pc, byte[] instruction, OperandDecoder operands, boolean swap) {
            super(pc, instruction, getOp1(operands, OperandDecoder.R32, swap), getOp2(operands, OperandDecoder.R32, swap));
        }

        public Adcl(long pc, byte[] instruction, OperandDecoder operands, int imm) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R32), new ImmediateOperand(imm));
        }

        public Adcl(long pc, byte[] instruction, Operand operand, int imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    public static class Adcq extends Adc {
        public Adcq(long pc, byte[] instruction, OperandDecoder operands) {
            this(pc, instruction, operands, false);
        }

        public Adcq(long pc, byte[] instruction, OperandDecoder operands, boolean swap) {
            super(pc, instruction, getOp1(operands, OperandDecoder.R64, swap), getOp2(operands, OperandDecoder.R64, swap));
        }

        public Adcq(long pc, byte[] instruction, OperandDecoder operands, long imm) {
            super(pc, instruction, operands.getOperand1(OperandDecoder.R64), new ImmediateOperand(imm));
        }

        public Adcq(long pc, byte[] instruction, Operand operand, long imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    @Override
    protected String[] disassemble() {
        return new String[]{"adc", operand1.toString(), operand2.toString()};
    }
}
