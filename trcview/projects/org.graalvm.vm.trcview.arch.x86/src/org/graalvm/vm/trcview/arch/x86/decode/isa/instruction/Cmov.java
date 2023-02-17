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

public abstract class Cmov extends AMD64Instruction {
    private final String name;
    private final Operand operand1;
    private final Operand operand2;

    protected Cmov(long pc, byte[] instruction, String name, Operand operand1, Operand operand2) {
        super(pc, instruction);
        this.name = name;
        this.operand1 = operand1;
        this.operand2 = operand2;

        setGPRReadOperands(operand2);
        setGPRWriteOperands(operand1);
    }

    public static abstract class Cmova extends Cmov {
        protected Cmova(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmova", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovaw extends Cmova {
        public Cmovaw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmoval extends Cmova {
        public Cmoval(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovaq extends Cmova {
        public Cmovaq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovae extends Cmov {
        protected Cmovae(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovae", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovaew extends Cmovae {
        public Cmovaew(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovael extends Cmovae {
        public Cmovael(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovaeq extends Cmovae {
        public Cmovaeq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovb extends Cmov {
        protected Cmovb(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovb", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovbw extends Cmovb {
        public Cmovbw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovbl extends Cmovb {
        public Cmovbl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovbq extends Cmovb {
        public Cmovbq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovbe extends Cmov {
        protected Cmovbe(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovbe", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovbew extends Cmovbe {
        public Cmovbew(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovbel extends Cmovbe {
        public Cmovbel(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovbeq extends Cmovbe {
        public Cmovbeq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmove extends Cmov {
        protected Cmove(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmove", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovew extends Cmove {
        public Cmovew(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovel extends Cmove {
        public Cmovel(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmoveq extends Cmove {
        public Cmoveq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovg extends Cmov {
        protected Cmovg(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovg", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovgw extends Cmovg {
        public Cmovgw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovgl extends Cmovg {
        public Cmovgl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovgq extends Cmovg {
        public Cmovgq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovge extends Cmov {
        protected Cmovge(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovge", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovgew extends Cmovge {
        public Cmovgew(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovgel extends Cmovge {
        public Cmovgel(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovgeq extends Cmovge {
        public Cmovgeq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovl extends Cmov {
        protected Cmovl(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovl", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovlw extends Cmovl {
        public Cmovlw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovll extends Cmovl {
        public Cmovll(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovlq extends Cmovl {
        public Cmovlq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovle extends Cmov {
        protected Cmovle(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovle", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovlew extends Cmovle {
        public Cmovlew(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovlel extends Cmovle {
        public Cmovlel(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovleq extends Cmovle {
        public Cmovleq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovne extends Cmov {
        protected Cmovne(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovne", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovnew extends Cmovne {
        public Cmovnew(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovnel extends Cmovne {
        public Cmovnel(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovneq extends Cmovne {
        public Cmovneq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovno extends Cmov {
        protected Cmovno(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovno", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovnow extends Cmovno {
        public Cmovnow(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovnol extends Cmovno {
        public Cmovnol(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovnoq extends Cmovno {
        public Cmovnoq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovnp extends Cmov {
        protected Cmovnp(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovnp", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovnpw extends Cmovnp {
        public Cmovnpw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovnpl extends Cmovnp {
        public Cmovnpl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovnpq extends Cmovnp {
        public Cmovnpq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovns extends Cmov {
        protected Cmovns(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovns", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovnsw extends Cmovns {
        public Cmovnsw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovnsl extends Cmovns {
        public Cmovnsl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovnsq extends Cmovns {
        public Cmovnsq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovo extends Cmov {
        protected Cmovo(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovo", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovow extends Cmovo {
        public Cmovow(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovol extends Cmovo {
        public Cmovol(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovoq extends Cmovo {
        public Cmovoq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovp extends Cmov {
        protected Cmovp(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovp", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovpw extends Cmovp {
        public Cmovpw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovpl extends Cmovp {
        public Cmovpl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovpq extends Cmovp {
        public Cmovpq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    public static abstract class Cmovs extends Cmov {
        protected Cmovs(long pc, byte[] instruction, OperandDecoder operands, int type) {
            super(pc, instruction, "cmovs", operands.getOperand2(type), operands.getOperand1(type));
        }
    }

    public static class Cmovsw extends Cmovs {
        public Cmovsw(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R16);
        }
    }

    public static class Cmovsl extends Cmovs {
        public Cmovsl(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R32);
        }
    }

    public static class Cmovsq extends Cmovs {
        public Cmovsq(long pc, byte[] instruction, OperandDecoder operands) {
            super(pc, instruction, operands, OperandDecoder.R64);
        }
    }

    @Override
    protected AssemblerInstruction disassemble() {
        return new AssemblerInstruction(name, operand1, operand2);
    }
}
