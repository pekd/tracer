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
package org.graalvm.vm.trcview.arch.x86.decode.isa;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.util.HexFormatter;

public abstract class AMD64Instruction {
    public final long pc;
    protected final byte[] instruction;

    protected Operand[] gprReadOperands = new Operand[0];
    protected Operand[] gprWriteOperands = new Operand[0];

    protected AMD64Instruction(long pc, byte[] instruction) {
        this.pc = pc;
        this.instruction = instruction;
    }

    protected void setGPRReadOperands(Operand... operands) {
        gprReadOperands = operands;
    }

    protected void setGPRWriteOperands(Operand... operands) {
        gprWriteOperands = operands;
    }

    protected abstract String[] disassemble();

    public Register[] getUsedGPRRead() {
        Set<Register> regs = new HashSet<>();
        // gpr read operands
        for (Operand operand : gprReadOperands) {
            for (Register reg : operand.getRegisters()) {
                regs.add(reg.getRegister());
            }
        }
        // memory access operands
        for (Operand operand : gprWriteOperands) {
            if (operand instanceof MemoryOperand) {
                for (Register reg : operand.getRegisters()) {
                    regs.add(reg.getRegister());
                }
            }
        }
        return regs.toArray(new Register[regs.size()]);
    }

    public Register[] getUsedGPRWrite() {
        Set<Register> regs = new HashSet<>();
        for (Operand operand : gprWriteOperands) {
            // only register operands can write to registers
            if (operand instanceof RegisterOperand) {
                RegisterOperand op = (RegisterOperand) operand;
                if (op.getRegister() == null) {
                    System.out.println(this);
                    throw new AssertionError();
                }
                regs.add(op.getRegister().getRegister());
            }
        }
        return regs.toArray(new Register[regs.size()]);
    }

    public boolean isControlFlow() {
        return false;
    }

    public int getSize() {
        return instruction.length;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(instruction, instruction.length);
    }

    public long getPC() {
        return pc;
    }

    protected long next() {
        return getPC() + getSize();
    }

    public String[] getDisassemblyComponents() {
        return disassemble();
    }

    public String getDisassembly() {
        String[] parts = disassemble();
        if (parts.length == 1) {
            return parts[0];
        } else {
            return parts[0] + "\t" + Stream.of(parts).skip(1).collect(Collectors.joining(","));
        }
    }

    private String printBytes() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < instruction.length; i++) {
            buf.append(' ');
            buf.append(HexFormatter.tohex(Byte.toUnsignedInt(instruction[i]), 2));
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return getDisassembly() + " ;" + printBytes();
    }
}
