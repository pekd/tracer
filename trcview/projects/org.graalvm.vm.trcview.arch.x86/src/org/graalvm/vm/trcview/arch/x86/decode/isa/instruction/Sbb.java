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

public abstract class Sbb extends AMD64Instruction {
    private final Operand operand1;
    private final Operand operand2;

    protected Sbb(long pc, byte[] instruction, Operand operand1, Operand operand2) {
        super(pc, instruction);
        this.operand1 = operand1;
        this.operand2 = operand2;

        setGPRReadOperands(operand1, operand2);
        setGPRWriteOperands(operand1);
    }

    private static Operand getOp1(OperandDecoder decoder, int type, boolean swap) {
        return swap ? decoder.getOperand2(type) : decoder.getOperand1(type);
    }

    private static Operand getOp2(OperandDecoder decoder, int type, boolean swap) {
        return swap ? decoder.getOperand1(type) : decoder.getOperand2(type);
    }

    public static class Sbbb extends Sbb {
        public Sbbb(long pc, byte[] instruction, OperandDecoder decoder) {
            this(pc, instruction, decoder, false);
        }

        public Sbbb(long pc, byte[] instruction, OperandDecoder decoder, boolean swap) {
            super(pc, instruction, getOp1(decoder, OperandDecoder.R8, swap), getOp2(decoder, OperandDecoder.R8, swap));
        }

        public Sbbb(long pc, byte[] instruction, OperandDecoder decoder, byte imm) {
            super(pc, instruction, decoder.getOperand1(OperandDecoder.R8), new ImmediateOperand(imm));
        }

        public Sbbb(long pc, byte[] instruction, Operand operand, byte imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    public static class Sbbw extends Sbb {
        public Sbbw(long pc, byte[] instruction, OperandDecoder decoder) {
            this(pc, instruction, decoder, false);
        }

        public Sbbw(long pc, byte[] instruction, OperandDecoder decoder, boolean swap) {
            super(pc, instruction, getOp1(decoder, OperandDecoder.R16, swap), getOp2(decoder, OperandDecoder.R16, swap));
        }

        public Sbbw(long pc, byte[] instruction, OperandDecoder decoder, short imm) {
            super(pc, instruction, decoder.getOperand1(OperandDecoder.R16), new ImmediateOperand(imm));
        }

        public Sbbw(long pc, byte[] instruction, Operand operand, short imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    public static class Sbbl extends Sbb {
        public Sbbl(long pc, byte[] instruction, OperandDecoder decoder) {
            this(pc, instruction, decoder, false);
        }

        public Sbbl(long pc, byte[] instruction, OperandDecoder decoder, boolean swap) {
            super(pc, instruction, getOp1(decoder, OperandDecoder.R32, swap), getOp2(decoder, OperandDecoder.R32, swap));
        }

        public Sbbl(long pc, byte[] instruction, OperandDecoder decoder, int imm) {
            super(pc, instruction, decoder.getOperand1(OperandDecoder.R32), new ImmediateOperand(imm));
        }

        public Sbbl(long pc, byte[] instruction, Operand operand, int imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    public static class Sbbq extends Sbb {
        public Sbbq(long pc, byte[] instruction, OperandDecoder decoder) {
            this(pc, instruction, decoder, false);
        }

        public Sbbq(long pc, byte[] instruction, OperandDecoder decoder, boolean swap) {
            super(pc, instruction, getOp1(decoder, OperandDecoder.R64, swap), getOp2(decoder, OperandDecoder.R64, swap));
        }

        public Sbbq(long pc, byte[] instruction, OperandDecoder decoder, long imm) {
            super(pc, instruction, decoder.getOperand1(OperandDecoder.R64), new ImmediateOperand(imm));
        }

        public Sbbq(long pc, byte[] instruction, Operand operand, long imm) {
            super(pc, instruction, operand, new ImmediateOperand(imm));
        }
    }

    @Override
    protected String[] disassemble() {
        return new String[]{"sbb", operand1.toString(), operand2.toString()};
    }
}
