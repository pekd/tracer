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
package org.graalvm.vm.x86.isa.instruction;

import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.IllegalInstructionException;
import org.graalvm.vm.x86.isa.Operand;
import org.graalvm.vm.x86.isa.OperandDecoder;
import org.graalvm.vm.x86.node.ReadNode;
import org.graalvm.vm.x86.node.WriteNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class Roundsd extends AMD64Instruction {
    private final Operand operand1;
    private final Operand operand2;
    private final byte imm;

    @Child private ReadNode readSrc;
    @Child private WriteNode writeDst;

    protected Roundsd(long pc, byte[] instruction, Operand operand1, Operand operand2, byte imm) {
        super(pc, instruction);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.imm = imm;

        setGPRReadOperands(operand2);
        setGPRWriteOperands(operand1);
    }

    public Roundsd(long pc, byte[] instruction, OperandDecoder operands, byte imm) {
        this(pc, instruction, operands.getAVXOperand2(128), operands.getAVXOperand1(128), imm);
    }

    @Override
    protected void createChildNodes() {
        ArchitecturalState state = getState();
        readSrc = operand2.createRead(state, next());
        writeDst = operand1.createWrite(state, next());
    }

    @Override
    public long executeInstruction(VirtualFrame frame) {
        if ((imm & 0x4) != 0) {
            CompilerDirectives.transferToInterpreter();
            throw new IllegalInstructionException(pc, getBytes(), "Only IMM8 mode is supported for ROUNDSD");
        }

        int mode = imm & 0x03;

        double value = readSrc.executeF64(frame);
        long result;

        switch (mode) {
            case 0: // round to nearest (even)
                result = Math.round(value);
                break;
            case 1: // round down (towards negative infinity)
                result = (long) Math.floor(value);
                break;
            case 2: // round up (towards positive infinity)
                result = (long) Math.ceil(value);
                break;
            case 3: // round toward zero (truncate)
                result = (long) value;
                break;
            default:
                CompilerDirectives.transferToInterpreter();
                throw new AssertionError();
        }

        writeDst.executeI64(frame, result);
        return next();
    }

    @Override
    protected String[] disassemble() {
        return new String[]{"roundsd", operand1.toString(), operand2.toString(), "0x" + Integer.toHexString(Byte.toUnsignedInt(imm))};
    }
}
