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
import org.graalvm.vm.trcview.arch.x86.decode.isa.IllegalInstructionException;
import org.graalvm.vm.trcview.arch.x86.decode.isa.Operand;
import org.graalvm.vm.trcview.arch.x86.decode.isa.OperandDecoder;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Type;

public abstract class Cmppd extends AMD64Instruction {
    private final Operand operand1;
    private final Operand operand2;

    private final String name;
    private final byte type;

    protected Cmppd(long pc, byte[] instruction, Operand operand1, Operand operand2, String name, byte type) {
        super(pc, instruction);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.name = name;
        this.type = type;

        setGPRReadOperands(operand1, operand2);
        setGPRWriteOperands(operand1);
    }

    public static Cmppd create(long pc, byte[] instruction, OperandDecoder operands, byte imm) {
        switch (imm & 0x7) {
            case 0:
                return new Cmpeqpd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
            case 1:
                return new Cmpltpd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
            case 2:
                return new Cmplepd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
            case 3:
                return new Cmpunordpd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
            case 5:
                return new Cmpnltpd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
            case 6:
                return new Cmpnlepd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
            case 7:
                return new Cmpordpd(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128));
        }
        throw new IllegalInstructionException(pc, instruction, "unknown type " + imm);
    }

    public static class Cmpeqpd extends Cmppd {
        protected Cmpeqpd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmpeqpd", (byte) 0);
        }
    }

    public static class Cmpltpd extends Cmppd {
        protected Cmpltpd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmpltpd", (byte) 1);
        }
    }

    public static class Cmplepd extends Cmppd {
        protected Cmplepd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmplepd", (byte) 2);
        }
    }

    public static class Cmpunordpd extends Cmppd {
        protected Cmpunordpd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmpunordpd", (byte) 3);
        }
    }

    public static class Cmpnltpd extends Cmppd {
        protected Cmpnltpd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmpnltpd", (byte) 5);
        }
    }

    public static class Cmpnlepd extends Cmppd {
        protected Cmpnlepd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmpnlepd", (byte) 6);
        }
    }

    public static class Cmpordpd extends Cmppd {
        protected Cmpordpd(long pc, byte[] instruction, Operand operand1, Operand operand2) {
            super(pc, instruction, operand1, operand2, "cmpordpd", (byte) 7);
        }
    }

    @Override
    protected AssemblerInstruction disassemble() {
        if (name == null) {
            return new AssemblerInstruction("cmppd", operand1, operand2, op(Type.NUMBER, Byte.toString(type), type));
        } else {
            return new AssemblerInstruction(name, operand1, operand2);
        }
    }
}
